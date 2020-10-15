(ns advocado.components.user-menu
  (:require [advocado.mui :as ui]
            [debux.cs.core :refer [clog]]
            [tincture.core :refer [>evt <sub]]
            [garden.units :refer [px]]
            [herb.core :refer [defgroup <class]]
            [reagent.core :as r]))

(declare styles)
(defgroup styles
  {:paper {:padding [[(px 7) 0 (px 7) 0]]}
   :item {:padding [[(px 7) (px 32) (px 7) (px 32)]]}})

(defn UserMenu []
  (let [anchor (r/atom nil)
        handle-close #(reset! anchor nil)
        handle-open #(reset! anchor (.-currentTarget %))]
    (fn []
      [:div
       [ui/IconButton {:color :inherit
                       :on-click handle-open
                       :aria-controls "customized-menu"
                       :aria-haspopup true}
        [ui/Icon "person"]]
       [ui/Menu {:open (some? @anchor)
                 :anchor-el @anchor
                 :on-close handle-close
                 :classes {:paper (<class styles :paper)}
                 :keep-mounted true
                 :get-content-anchor-el nil
                 :anchor-origin #js {:vertical "bottom"
                                     :horizontal "center"}}
        [ui/MenuItem {:class (<class styles :item)
                      :on-click handle-close}
         "Profile"]
        [ui/Divider {:variant :middle}]
        [ui/Link {:href "/#/account"
                  :color :inherit
                  :underline :none}
         [ui/MenuItem {:class (<class styles :item)
                       :on-click handle-close}
          "Account Settings"]]
        [ui/MenuItem {:class (<class styles :item)
                      :on-click (partial (juxt handle-close #(>evt [:logout/submit])))}
         "Logout"]]
       ])))
