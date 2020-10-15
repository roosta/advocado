(ns advocado.auth
  (:require [cljs-time.coerce :refer [from-long]]
            [cljs-time.core :refer [in-minutes in-seconds interval now time-now after?]]
            [ajax.core :as ajax]
            [advocado.interceptors :refer [check-spec]]
            [advocado.validation :refer [default-db reset-db]]
            [advocado.token :refer [authenticated? current-user]]
            [debux.cs.core :refer [clog]]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [goog.crypt.base64 :as base64]))

; Authentication {{{

(rf/reg-sub
 :auth/current-token
 (fn [db]
   (get db :auth/current-token)))

; Use this sub to pass a token, instead of subbing to current token.  Used for
; reset password form.
(rf/reg-sub
  :auth/valid-path-token
  :<- [:nav/path-token]
  authenticated?)

(rf/reg-sub
  :auth/authenticated
  :<- [:auth/current-token]
  authenticated?)

; }}}
; User {{{

(rf/reg-sub
  :user/current
  :<- [:auth/current-token]
  current-user)

(rf/reg-sub
  :user/full-name
  :<- [:user/current]
  (fn [{:keys [user]}]
    (str (:first_name user) " " (:last_name user))))

(rf/reg-sub
  :user/username
  :<- [:user/current]
  (fn [{:keys [user]}]
    (:username user)))

(rf/reg-sub
  :user/email
  :<- [:user/current]
  (fn [{:keys [user]}]
    (:email user)))


; }}}
; Login {{{

(rf/reg-sub
 :login/state
 (fn [db]
   (-> db :login/state)))

(rf/reg-event-db
 :login.state/set
 (fn [db [_ message]]
   (assoc db :login/state message)))

(rf/reg-event-fx
 :login/success
 (fn [{:keys [db]} [_ {:keys [token]}]]
   {:db (assoc db :auth/current-token token)
    :local-store/assoc! [:token token]
    :router/start! nil}))

(rf/reg-event-db
 :login/failed
 [check-spec]
 (fn [db [_ result]]
   (assoc db :login/state (-> result :response :message))))

(rf/reg-event-fx
 :login/submit
 (fn [{:keys [db]} [_ data]]
   {:db db
    :http-xhrio {:method          :post
                 :uri             "/login"
                 :timeout         5000
                 :params          data
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:login/success]
                 :on-failure      [:login/failed]}}))


; }}}
; Logout {{{

(rf/reg-event-fx
  :logout/submit
  (fn [{:keys [db]} [_]]
    {:db (reset-db db)
     :local-store/dissoc! :token
     :router/start! nil
     :dispatch [:nav/navigate! :login]}))

; }}}
; Signup {{{

(rf/reg-sub
 :signup/state
 (fn [db]
   (-> db :signup/state)))

(rf/reg-sub
  :signup.state/data
  :<- [:signup/state]
  (fn [state]
    (:data state)))

(rf/reg-sub
  :signup.state/message
  :<- [:signup/state]
  (fn [state]
    (:message state)))

(rf/reg-event-fx
 :signup/submit
 (fn [{:keys [db]} [_ data]]
   {:db db
    :http-xhrio {:method          :post
                 :uri             "/signup"
                 :timeout         5000
                 :params          data
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:signup/success]
                 :on-failure      [:signup/failed]}}))

(rf/reg-event-fx
  :signup/success
  (fn [{:keys [db]} [_ {:keys [token]}]]
    (.log js/console "Signup success")
    {:db (assoc db :auth/current-token token)
     :local-store/assoc! [:token token]
     :router/start! nil
     :dispatch [:nav/navigate! :home]}))

(rf/reg-event-db
  :signup/failed
  [check-spec]
  (fn [db [_ result]]
    (let [data (-> result :response :data set)
          msg (-> result :response :message)]
      (assoc db :signup/state {:data data
                               :message msg}))))

(rf/reg-event-db
  :signup.state.data/remove
  (fn [db [_ v]]
    (update-in db [:signup/state :data] disj v)))

(rf/reg-event-db
  :signup.state.data/reset
  (fn [db [_ v]]
    (assoc-in db [:signup/state :data] #{})))

; }}}
; Forgot password {{{

(rf/reg-sub
 :forgot-password/state
 (fn [db]
   (-> db :forgot-password/state)))

(rf/reg-event-db
 :forgot-password.state/set
 (fn [db [_ message]]
   (assoc db :forgot-password/state message)))

(rf/reg-event-db
 :forgot-password/failed
 [check-spec]
 (fn [db [_ result]]
   (assoc db :forgot-password/state [false (-> result :response :message)])))

(rf/reg-event-db
 :forgot-password/success
 [check-spec]
 (fn [db [_ result]]
   (assoc db :forgot-password/state [true (-> result :message)])))

(rf/reg-event-fx
 :forgot-password/submit
 (fn [{:keys [db]} [_ data]]
   {:db db
    :http-xhrio {:method          :post
                 :uri             "/forgot-password"
                 :timeout         5000
                 :params          data
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:forgot-password/success]
                 :on-failure      [:forgot-password/failed]}}))


; }}}
; Reset password {{{

(rf/reg-sub
 :reset-password/state
 (fn [db]
   (-> db :reset-password/state)))


(rf/reg-event-db
 :reset-password.state/set
 (fn [db [_ message]]
   (assoc db :reset-password/state message)))


(rf/reg-event-db
 :reset-password/failed
 [check-spec]
 (fn [db [_ result]]
   (assoc db :reset-password/state [false (-> result :response :message)])))

(rf/reg-event-db
 :reset-password/success
 [check-spec]
 (fn [db [_ result]]
   (assoc db :reset-password/state [true (-> result :message)])))

(rf/reg-event-fx
 :reset-password/submit
 (fn [{:keys [db]} [_ data]]
   {:db db
    :http-xhrio {:method          :post
                 :uri             "/reset-password"
                 :timeout         5000
                 :params          data
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:reset-password/success]
                 :on-failure      [:reset-password/failed]}}))


;;}}}

;  vim: set ts=2 sw=2 tw=0 fdm=marker et :
