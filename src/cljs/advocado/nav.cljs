(ns advocado.nav
  (:require [re-frame.core :as rf]
            [reitit.core :as r]
            [ajax.core :as ajax]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.controllers :as rfc]
            [advocado.views.home :refer [Home]]
            [advocado.views.terms :refer [Terms]]
            [advocado.views.privacy :refer [Privacy]]
            [advocado.views.account :as account]
            [advocado.views.profile :refer [Profile]]
            [advocado.views.auth :refer [Login ForgotPass ResetPass Signup]]
            [tincture.core :refer [<sub]]))

; Router {{{

(defn merge-routers [& routers]
  (r/router
    (apply merge (map r/routes routers))
    (apply merge (map r/options routers))))

(comment
  [true {:name :redirect
         :controllers [{:start #(rf/dispatch [:nav/navigate! :home])}]}])

(def common-router
  (r/router
    [["/terms" {:name :terms
                :view #'Terms
                :controllers [{:start #(rf/dispatch [:page.terms/init])}]}]
     ["/privacy" {:name :privacy
                  :view #'Privacy
                  :controllers [{:start #(rf/dispatch [:page.privacy/init])}]}]]))

(def account-router
  (r/router
    [["/account" {:name :account-settings
                  :view #'account/Account}]
     ["/account/profile" {:name :account-profile
                          :view #'account/Profile}]
     ["/account/password" {:name :account-password
                           :view #'account/Password}]]))

(def logged-out-router
  (merge-routers
    (r/router
      [["/" {:name        :login
             :view        #'Login}]
       ["/forgot-password" {:name :forgot-pass
                            :view #'ForgotPass}]
       ["/signup" {:name :signup
                   :view #'Signup}]
       ["/reset-password/:token" {:name :reset-pass
                                  :view #'ResetPass}]])
    common-router))

(def logged-in-router
  (merge-routers
    (r/router
      (into [["/" {:name        :home
                   :controllers [{:start (fn [_] (rf/dispatch [:page.home/init]))}]
                   :view        #'Home}]
             ["/profile/:username" {:name :profile
                                    :view #'Profile}]]))
    account-router
    common-router))

(defn navigate! [match _]
  (rf/dispatch [:nav/navigate match]))

(defn start! []
  (let [auth (<sub [:auth/authenticated])
        router (if auth logged-in-router logged-out-router)]
    (rfe/start!
     router
     navigate!
     {})))

(rf/reg-event-fx
 :router/start
 (fn [{:keys [db]}]
   {:db db
    :router/start! nil}))

(rf/reg-fx :router/start!  start!)

; }}}
; Navigation {{{

(rf/reg-event-db
  :nav/navigate
  (fn [db [_ match]]
    (let [old-match (:nav/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :nav/route new-match))))

(rf/reg-fx
  :nav/navigate-fx!
  (fn [[k & [params query]]]
    (rfe/push-state k params query)))

(rf/reg-event-fx
  :nav/navigate!
  (fn [_ [_ url-key params query]]
    {:nav/navigate-fx! [url-key params query]}))

(rf/reg-event-db
  :nav/set-loading
  (fn [db [_ v]]
    (assoc db :nav/loading v)))


(rf/reg-sub
  :nav/route
  (fn [db _]
    (-> db :nav/route)))

(rf/reg-sub
  :nav/page-id
  :<- [:nav/route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :nav/params
  :<- [:nav/route]
  (fn [route]
    (:parameters route)))

(rf/reg-sub
  :nav/path-params
  :<- [:nav/params]
  (fn [params]
    (:path params)))

(rf/reg-sub
  :nav/path-token
  :<- [:nav/path-params]
  (fn [params]
    (:token params)))

(rf/reg-sub
  :nav/page
  :<- [:nav/route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :nav/loading
  (fn [db]
    (-> db :nav/loading)))

;}}}
; Initializers {{{

(rf/reg-event-fx
  :page.home/init
  (fn [{:keys [db]} []]
    {:db (assoc db :nav/loading true)
     :http-xhrio {:method :get
                  :uri "/docs"
                  :response-format (ajax/raw-response-format)
                  :on-success [:docs/set :home]
                  :on-failure [:ajax/set-error]}}))

(rf/reg-event-fx
  :page.terms/init
  (fn [{:keys [db]} []]
    {:db (assoc db :nav/loading true)
     :http-xhrio {:method          :get
                  :uri             "/terms"
                  :response-format (ajax/raw-response-format)
                  :on-success       [:docs/set :terms]}}))

(rf/reg-event-fx
  :page.privacy/init
  (fn [{:keys [db]} []]
    {:db (assoc db :nav/loading true)
     :http-xhrio {:method          :get
                  :uri             "/privacy"
                  :response-format (ajax/raw-response-format)
                  :on-success       [:docs/set :privacy]}}))
;}}}

;  vim: set ts=2 sw=2 tw=0 fdm=marker et :
