(ns advocado.routes.home
  (:require
   [advocado.layout :as layout]
   [advocado.db.core :as db]
   [clojure.java.io :as io]
   [advocado.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page
  "home-page handler that returns our SPA inital HTML"
  [request]
  (layout/render request "home.html"))

(defn home-routes
  "These routes are when user is authenticated using auth-token middleware, the
  only exception is the root route, which returns our SPA so that shouldn't be
  wrapped in auth-token"
  []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:middleware [middleware/wrap-auth-token]
             :get (fn [_]
                    (-> (response/ok (-> "docs/home.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]])
