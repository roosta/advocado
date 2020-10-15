(ns advocado.interceptors
  (:require
    [re-frame.core :as rf]
    [clojure.spec.alpha :as s]
    [debux.cs.core :refer [clog]]
    [advocado.validation :refer [reset-db]]
    [advocado.token :refer [valid-token?]]
    [expound.alpha :as expound]
    ))

(defn check-spec-and-throw
  "throw an exception if db doesn't match the spec"
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (expound/expound a-spec db)) {}))))

#_(defn check-token-and-redirect
  "Checks token on certain db manipulations, making sure token is still valid"
  [db]
  (let [token (:auth/current-token db)]
    (when-not (valid-token? token)
      (.error js/console "Expired or invalid token, redirecting to login")
      (rf/dispatch [:logout/submit])
      db)))

;; Check token after each event handler has finished, will throw error and redirect to login
#_(def check-token (rf/after (partial check-token-and-redirect)))

(def check-token
  ^{:doc "Checks token on certain db manipulations, making sure token is still valid"}
  (re-frame.core/->interceptor
    :id :check-token
    :before  (fn [context]
               (let [token (-> context :coeffects :db :auth/current-token)]
                 (if (valid-token? token)
                   context
                   (do
                     (.error js/console "Expired or invalid token, redirecting to login")
                     (rf/dispatch [:logout/submit])))))))

;; This interceptor is run after each event handler has finished, and it checks
;; app-db against a spec.
(def check-spec (rf/after (partial check-spec-and-throw :advocado.validation/db)))


