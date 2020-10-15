(ns advocado.auth
  (:require [advocado.db.core :as db]
            [advocado.config :refer [env]]
            [debux.core :refer [dbg]]
            [java-time.local :as l]
            [clojure.tools.logging :as log]
            [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [buddy.sign.util :as util]
            [buddy.core.keys :as ks]
            [clj-time.core :as t]
            [clojure.java.io :as io]))

(defn user-id->role-names
  "Takes a user-id and returns a vector of role names for use in frontend"
  [user-id]
  (mapv :name (db/get-user-roles user-id)))

(defn role-names->ids
  "Takes a collection of role names and return their ids"
  [role-names]
  (map #(db/name->role-id {:name %}) role-names))

(defn auth-user
  "Return user response either authed or unauthed depending on user input.
   Check hashed password against whats in the db to auth."
  [credentials]
  (let [unauthed [false {:message "Invalid email or password."}]]
    (if-let [user-id (db/email->id {:email (-> credentials :email)})]
      (if-let [user (db/get-user user-id)]
        (if (hashers/check (:password credentials) (:password user))
          [true {:user (into (dissoc user :password)
                             (db/update-timestamp! (assoc user-id :last_login (l/local-date-time))))
                 :roles (user-id->role-names user-id)}]
          (do
            (log/debug (str "Failed to authenticate user " (:email credentials) ". Password doesn't match."))
            unauthed))
        (do
          (log/debug (str "Failed to get user with id: " user-id))
          unauthed))
      (do
        (log/debug (str "Failed to get userid for email: " (:email credentials)))
        unauthed)
      )))

(defn privkey
  "Helper function to read our private key."
  []
  (log/debug "Accessed private key")
  (ks/private-key
    (io/resource (:privkey env))
    (:passphrase env)))

(defn pubkey
  "Helper function to read our public key."
  []
  (log/debug "Accessed public key")
  (ks/public-key
   (io/resource (:pubkey env))))

(defn unsign-token
  "Validate token by unsigning, done on each request."
  [token]
  (jwt/unsign token (pubkey) {:alg :rs256}))

(defn create-auth-token
  "Sign and return a token to pass with response based on user map returned from `auth-user`"
  [credentials]
  (let [[ok? res] (auth-user credentials)]
    (if ok?
      (let [exp (-> (t/plus (t/now) (t/days 1)) (util/to-timestamp))  ; Sets a timestamp for when the token expires
            token (jwt/sign res (privkey) {:alg :rs256 :exp exp})]
        (log/info (str "Created auth token for user " (:email credentials)))
        [true {:token token}])
      [false res])))

(defn check-email
  "Checks email and return a response if email is present in db"
  [params]
  (let [invalid [false {:message "Can't find an account with that information."}]]
    (if (db/email->id params)
      [true params]
      (do
        (log/debug (str "Failed to retrieve account with email: " (:email params)))
        invalid))))

(defn change-password!
  "Change password in db, used from forgot/reset password"
  [email password]
  (let [pass (hashers/derive password {:alg :pbkdf2+sha256})
        user-id (db/email->id {:email email})]
    (db/update-password! (assoc user-id :password pass))))

(defn create-reset-password-token
  "Creates a new token for password reset."
  [params]
  (let [[ok? res] (check-email params)]
    (if ok?
      (let [exp (-> (t/plus (t/now) (t/minutes 10))
                    (util/to-timestamp))
            token (jwt/sign res (privkey) {:alg :rs256 :exp exp})]
        (log/info (str "Created reset password token for " (:email params)))
        [true {:token token}])
      [false res])))

(defn create-user
  "Creates a new user, returns an auth token on success"
  [{:keys [first-name last-name username email password roles]}]
  (let [hashed (hashers/derive password {:alg :pbkdf2+sha256})
        user-id (db/create-user! {:first_name first-name
                                  :last_name last-name
                                  :username username
                                  :email email
                                  :is_active false
                                  :password hashed})
        role-ids (role-names->ids roles)]
    (doseq [id role-ids]
      (db/add-role! (into user-id id)))
    #_(db/update-timestamp! (assoc user-id :last_login (l/local-date-time)))
    (create-auth-token {:email email :password password})))

