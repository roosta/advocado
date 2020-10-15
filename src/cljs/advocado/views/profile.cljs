(ns advocado.views.profile
  (:require [herb.core :refer [defgroup <class]]
            [tincture.core :refer [<sub]]
            [advocado.mui :as ui]))

(defgroup styles
  {:paper {}})

(defn Profile []
  (let [path-params (<sub [:nav/path-params])]
    (.log js/console path-params)
    [ui/Grid {:container true
              :justify :center
              :align-items :center}
     [ui/Grid {:item true
               :lg 6
               :md 8
               :xs 12}
      [ui/Paper
       [ui/Typography
        (str "Hello " )]
       ]]]))
