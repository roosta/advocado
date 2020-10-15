(ns advocado.components.list-item-link
  (:require [reagent.core :as r]
            [advocado.mui :as ui]))

(defn ListItemLink [props]
  (into [ui/ListItem (merge
                       {:button true
                        :component :a}
                       props)]
        (r/children (r/current-component))))
