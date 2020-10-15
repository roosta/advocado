(ns advocado.routes.auth
  (:require [advocado.middleware :as middleware]
            [java-time.local :as l]
            [advocado.validation]
            [debux.core :refer [dbg]]
            [clojure.tools.logging :as log]
            [advocado.email :refer [send-message]]
            [clojure.spec.alpha :as s]
            [hiccup.core :refer [html]]
            [advocado.db.core :as db]
            [advocado.auth :as auth]))

(defn create-email-body [url]
  (html
    [:div
     [:h2 "Reset Password"]
     [:p
      "Please use the following link to reset your Advocado password. The link is valid for 10 minutes."]
     [:a {:href url} "Password Reset Link"]]))

(defn reset-password-url [origin token]
  (str origin "/#/reset-password/" token))

(defn login-handler
  "Login handler function, create an auth token and return 201, unless credentials is not valid
   Also adds lastest timestamp to user table last_login column."
  [req]
  (let [parsed (s/conform :auth/login-form (:params req))]
    (if (= parsed ::s/invalid)
      (do
        (log/debug (str "Login failed, unable to parse input data for user: " (:email (:params req))))
        {:status 403 :body {:message "Invalid input."}})
      (let [[ok? res] (auth/create-auth-token parsed)]
        (if ok?
          (do
            (log/info (str (:email parsed) " succesfully authenticated"))
            {:status 200 :body res})
          (do
            (log/debug (str "Could not create auth token: " res))
            {:status 401 :body res}))))))

(defn signup-handler
  [req]
  (let [parsed (s/conform :auth/signup-form (:params req))]
    (if (= parsed ::s/invalid)
      (do
        (log/debug "Signup failed, unable to parse input data for new signup:" (dissoc (:params req) :password))
        {:status 403 :body {:message "Something went wrong, unable to parse form fields."}})
      (let [data (cond-> []
                   (db/email->id {:email (:email parsed)}) (conj :email)
                   (db/username->id {:username (:username parsed)}) (conj :username))]

        (if (seq data)
          {:status 409 :body {:data data}}
          (let [[ok? res] (auth/create-user parsed)]
            (if ok?
              (do
                (log/info "New user created with email: " (:email parsed))
                {:status 200 :body res})
              (do
                (log/error (str "Failed to create user " (:email parsed) " in database!"))
                {:status 400 :body res}))))))))


(defn forgot-password-handler
  [req]
  (let [parsed (s/conform :auth/forgot-pass-form (:params req))]
    (if (= parsed ::s/invalid)
      (do
        (log/debug (str "Failed to parse forgot-password input params for user " (:email (:params req))))
        {:status 403 :body {:message "Please provide a valid email."}})
      (let [[ok? res] (auth/create-reset-password-token parsed)]
        (if ok?
          (do
            (try (send-message
                   "account-service@advocado.com"
                   (:email parsed)
                   "Advocado Password Reset"
                   (reset-password-url (get-in req [:headers "origin"]) (:token res)))
                 (catch Throwable _
                   (log/error "Failed to dispatch email to: " (:email parsed))
                   {:status 403 :body {:message "Something went wrong, failed to dispatch reset password email"}}))
            {:status 202 :body {:message "Email with instructions for password reset is sent."}})
          {:status 403 :body res})))))

(defn reset-password-handler
  "Handles changing password for user via reset-password views"
  [req]
  (let [parsed (s/conform :auth/reset-pass-request (:params req))]
    (if (= parsed ::s/invalid)
      (do
        (log/debug "Failed to parse reset-pass-request")
        {:status 403 :body {:message "Input invalid, make sure that the password is repeated correctly."}})
      (let [token* (try (auth/unsign-token (:token parsed))
                        (catch Throwable t
                          (log/debug (str "Failed to unsign token " (:token parsed)))
                          (:cause (Throwable->map t))))]
        (if (:email token*)
          (if (s/valid? :auth/token token*)
            (let [changed (try (auth/change-password! (:email token*) (:password parsed))
                               (catch Throwable _
                                 (log/debug (str "Failed to change password for " (:email token*)))
                                 false
                                 ))]
              (if (= changed 1)
                (do
                  (log/info "Password successfully changed for user " (:email token*))
                  {:status 202 :body {:message "Password successfully changed."}})
                {:status 403 :body {:message "Failed to change password, something went wrong"}}))
            {:status 403 :body {:message "Token failed to pass validation, contact admins"}})
          {:status 403 :body {:message "No email associated with token"}})))))

(defn auth-routes
  "Auth routes, wraps csrf (might not be needed due to using signed token, but for good measure)"
  []
  [""
   {:middleware [middleware/wrap-formats
                 middleware/wrap-csrf]}
   ["/login" {:post login-handler}]
   ["/signup" {:post signup-handler}]
   ["/forgot-password" {:post forgot-password-handler}]
   ["/reset-password" {:post reset-password-handler}]])

