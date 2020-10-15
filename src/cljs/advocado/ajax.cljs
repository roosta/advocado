(ns advocado.ajax
  (:require
    [ajax.core :as ajax]
    [luminus-transit.time :as time]
    [advocado.validation :refer [reset-db]]
    [advocado.interceptors :refer [check-token]]
    [debux.cs.core :refer [clog]]
    [tincture.core :refer [<sub]]
    [cognitect.transit :as transit]
    [re-frame.core :as rf]))

(defn local-uri? [{:keys [uri]}]
  (not (re-find #"^\w+?://" uri)))

(defn default-headers [request]
  (let [token (<sub [:auth/current-token])]
    (if (local-uri? request)
      (-> request
          (update :headers #(merge {"x-csrf-token" js/csrfToken
                                    "x-token" token} %)))
      request)))

;; injects transit serialization config into request options
(defn as-transit [opts]
  (merge {:raw             false
          :format          :transit
          :response-format :transit
          :reader          (transit/reader :json time/time-deserialization-handlers)
          :writer          (transit/writer :json time/time-serialization-handlers)}
         opts))

(defn load-interceptors! []
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name "default headers"
                               :request default-headers})))

(rf/reg-event-fx
  :ajax/set-error
  (fn [{:keys [db]} [_ error]]
    (if (= (:status error) 403)
      {:db (-> (reset-db db)
               (assoc :ajax/error error))
       :local-store/dissoc! :token
       :router/start! nil
       :dispatch [:nav/navigate! :login] }
      {:db (assoc db :ajax/error error)})))

(rf/reg-sub
  :ajax/error
  (fn [db _]
    (:ajax/error db)))


