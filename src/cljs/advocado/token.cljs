(ns advocado.token
  (:require [cljs-time.coerce :refer [from-long]]
            [cljs-time.core :refer [in-minutes in-seconds interval now time-now after?]]
            [ajax.core :as ajax]
            [debux.cs.core :refer [clog]]
            [clojure.string :as str]
            [goog.crypt.base64 :as base64]))

(defn decode [s]
  (js->clj (->> s
                base64/decodeString
                (.parse js/JSON))
           :keywordize-keys true))

(defn decode-token [token]
  (let [[header payload _] (str/split token #"\.")]
    {:header (decode header)
     :payload (decode payload)}))

(defn valid-token? [token]
  (when token
    (let [exp (from-long (* 1000 (-> (decode-token token) :payload :exp)))
          valid? (after? exp (time-now))]
      (if valid?
        (in-seconds (interval (time-now) exp))
        false))))

(defn current-user [token]
  (when token
    (update (js->clj (:payload (decode-token token)) :keywordize-keys true)
            :roles #(set (map (fn [role] (keyword role)) %)))))

(defn authenticated? [token]
  (if token
    (try (valid-token? token)
         (catch js/Error _
           false))
    false))

