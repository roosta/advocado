(ns advocado.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[advocado started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[advocado has shut down successfully]=-"))
   :middleware identity})
