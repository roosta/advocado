(ns advocado.color
  (:require [cljsjs.material-ui]))

(defonce colors (js->clj (aget js/MaterialUI "colors")))

(defn color
  "return color hex for a given name and material color code.
  http://www.material-ui.com/#/customization/colors
  If only name is proveded as an argument default to returning 500"
  ([color code] (get-in colors [(name color) (str code)]))
  ([color] (color (name color) "500")))

(defn emphasize
  [color coefficient]
  (js/MaterialUIStyles.emphasize color coefficient))

(defn fade
  [color value]
  (js/MaterialUIStyles.fade color value))

(defn darken
  [color coefficient]
  (js/MaterialUIStyles.darken color coefficient))

(defn lighten
  [color coefficient]
  (js/MaterialUIStyles.lighten color coefficient))

