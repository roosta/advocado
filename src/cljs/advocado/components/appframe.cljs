(ns advocado.components.appframe
  (:require [advocado.color :as color]
            [debux.cs.core :refer [clog]]
            [advocado.mui :as ui]
            [advocado.components.appbar :refer [AppBar]]
            [herb.core :refer [<class]]
            [tincture.core :refer [<sub]]
            [garden.units :refer [px]]
            [reagent.core :as r]))

(defn style []
  (let [sm-down? (<sub [:tincture/breakpoint-down :sm]) ]
    {:padding [[(if sm-down? (px 72) (px 80)) 0 (px 16)]] ; 56 + 16, 64 + 16
     :background-color (color/color :grey 100)
     :min-height "100%"
     :display :flex}))

(defn AppFrame []
  (r/create-class
    {:display-name "AppFrame"
     :reagent-render
     (fn []
       (let [theme (<sub [:theme/active])]
         [ui/StylesProvider {:inject-first true}
          [ui/MuiThemeProvider {:theme theme}
           (into
             [:div {:class (<class style)}
              [AppBar]]
             (r/children (r/current-component)))]]))}))

