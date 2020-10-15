(ns advocado.local-store
  (:require [re-frame.core :as rf]
            [hodgepodge.core :refer [local-storage]]))

(rf/reg-fx
 :local-store/assoc!
 (fn [[k data]]
   (assoc! local-storage k data)))

(rf/reg-fx
 :local-store/dissoc!
 (fn [k]
   (dissoc! local-storage k)))

(rf/reg-cofx
 :local-store
 (fn [cofx local-store-key]
   (assoc cofx :local-store (get local-storage local-store-key))))
