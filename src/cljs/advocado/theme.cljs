(ns advocado.theme
  (:require [re-frame.core :as rf]
            [goog.object :as gobj]
            [reagent.core :as r]))

(defn create-theme
  "Creates a basic theme, either using default values or supplied"
  [theme]
  (.createMuiTheme js/MaterialUI (clj->js theme)))

(rf/reg-event-fx
 :theme/initialize
 (fn [{:keys [db]} [_ theme]]
   (let [theme* (create-theme theme)]
     {:db (assoc db :theme/active theme*)})))

;; subscribe to current theme, mainly used in theme-provider theme prop
(rf/reg-sub
 :theme/active
 (fn [db]
   (:theme/active db)))

(rf/reg-event-fx
 :theme/set
 (fn [{:keys [db]} [_ theme]]
   (let [theme* (create-theme theme)]
     {:db (assoc db :theme/active theme*)})))

(rf/reg-sub
 :theme/type
 :<- [:theme/active]
 (fn [theme]
   (when theme
     (gobj/getValueByKeys theme "palette" "type"))))
