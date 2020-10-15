(ns advocado.temp
  (:require [re-frame.core :as rf]
            [advocado.interceptors :refer [check-token]]))

(rf/reg-event-db
  :temp/remove-token
  (fn [db]
    (assoc db :auth/current-token nil)))

(rf/reg-event-db
  :temp/add-something
  (fn [db]
    (assoc db :some :thing)))

(rf/reg-event-fx
  :temp/check-token
  [check-token]
  (fn [{:keys [db]}]
    {:db (assoc db :temp :test)
     :dispatch [:temp/add-something]}
      ))
