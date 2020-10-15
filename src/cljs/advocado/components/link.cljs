(ns advocado.components.link
  (:require [tincture.core :refer [<sub]]
            [reagent.core :as r]
            [debux.cs.core :refer [clog]]
            [advocado.color :as c]
            [herb.core :refer [<class]]
            [advocado.mui :as ui]
            [goog.object :as gobj]))

(defn style [color-key color-direction hover]
  (let [theme (<sub [:theme/active])
        colors {:primary (gobj/getValueByKeys theme "palette" "primary" "main")
                :secondary (gobj/getValueByKeys theme "palette" "secondary" "main")
                :textPrimary (gobj/getValueByKeys theme "palette" "text" "primary")
                :textSecondary (gobj/getValueByKeys theme "palette" "text" "secondary")
                :error (gobj/getValueByKeys theme "palette" "error" "main")
                :inherit "inherit"
                :initial "initial"}
        color (get colors (keyword color-key))
        coefficient 0.3
        accent (cond
                 (or (= color "inherit") (= color "initial")) (:textPrimary colors)
                 (= color-key :textSecondary) (:textPrimary colors)
                 (= color-key :textPrimary) (:textSecondary colors)
                 (= color-direction :lighten) (c/lighten color coefficient)
                 (= color-direction :darken) (c/darken color coefficient))]
    (cond-> {:color color}
      hover (with-meta {:pseudo {:hover {:color accent}}}))))

(defn Link [{:keys [class color color-direction hover]
             :as props
             :or {color :primary
                  hover true
                  color-direction :darken}}]
  (into [ui/Link (merge
                   (dissoc props :color-direction :hover)
                   {:class (let [local (<class style color color-direction hover)]
                             (if (vector? class)
                               (conj class local)
                               [class local]))})]
        (r/children (r/current-component))))

