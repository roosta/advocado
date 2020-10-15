(ns advocado.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [advocado.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[advocado started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[advocado has shut down successfully]=-"))
   :middleware wrap-dev})
