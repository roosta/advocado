(ns advocado.docs
  (:require [re-frame.core :as rf]))

(rf/reg-event-fx
  :docs/set
  (fn [{:keys [db]} [_ k docs]]
    {:db (-> (assoc-in db [:docs k] docs)
             (assoc :nav/loading false))}))

(rf/reg-sub
  :docs/get
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :docs/home
  :<- [:docs/get]
  (fn [docs]
    (get docs :home)))

(rf/reg-sub
  :docs/terms
  :<- [:docs/get]
  (fn [docs]
    (get docs :terms)))

(rf/reg-sub
  :docs/privacy
  :<- [:docs/get]
  (fn [docs]
    (get docs :privacy)))

