(ns advocado.components.appbar
  (:require [advocado.mui :as ui]
            [debux.cs.core :refer [clog]]
            [garden.color :refer [darken]]
            [advocado.components.user-menu :refer [UserMenu]]
            [advocado.color :as c]
            [herb.core :refer [defgroup <class]]
            [tincture.core :refer [<sub]]
            [garden.units :refer [px]]))

(declare styles)
(defgroup styles
  (let [theme (<sub [:theme/active])
        title-color "#ffffff"]
    {:icon-button {:margin-right (px (.spacing theme 2))}
     :title
     ^{:pseudo {:hover {:color (c/darken title-color 0.1)}}}
     {:flex-grow 1
      :color title-color
      :cursor :pointer}}))

(defn AppBar []
  (let [authenticated? (<sub [:auth/authenticated])]
    [ui/AppBar
     [ui/Toolbar
      [ui/IconButton {:edge "start" :class (<class styles :icon-button)
                      :color :inherit
                      :aria-label :menu}
       [ui/Icon "menu"]]
      [ui/Link {:variant :h6
                :href "/#/"
                :underline :none
                :class (<class styles :title)}
       "Advocado"]
      (if authenticated?
        [UserMenu]
        [:div
         [ui/Button {:color :inherit
                     :href "/#/"}
          "Login"]
         [ui/Button {:variant :contained
                     :href "/#/signup"
                     :color :secondary}
          "Sign up"]])]]))

