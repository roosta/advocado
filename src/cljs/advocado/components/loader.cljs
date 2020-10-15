(ns advocado.components.loader
  (:require [advocado.mui :as ui]
            [herb.core :refer [defgroup <class]]))

(defgroup styles
  {:container {}})

(defn Loader
  "Loader component that is rendered if :nav/loading is true"
  []
  [ui/Grid {:container true
            :justify :center
            :align-items :center
            :class (<class styles :container)}
   [ui/CircularProgress {:disable-shrink true}]])

