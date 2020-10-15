(ns advocado.routes.common
  (:require
   [clojure.java.io :as io]
   [advocado.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn common-routes
  "These are some common routes that are available independent of auth status"
  []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/terms" {:get (fn [_]
                     (-> (response/ok (-> "docs/terms.md" io/resource slurp))
                         (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/privacy" {:get (fn [_]
                     (-> (response/ok (-> "docs/privacy.md" io/resource slurp))
                         (response/header "Content-Type" "text/plain; charset=utf-8")))}]])

