(ns advocado.views.home
  (:require [markdown.core :refer [md->html]]
            [herb.core :refer [defgroup <class]]
            [garden.units :refer [px]]
            [tincture.core :refer [<sub]]
            [advocado.mui :as ui]
            [re-frame.core :as rf]))

(defgroup styles
  {:paper {}})

(defn Home []
  (when-let [docs (<sub [:docs/home])]
    [ui/Grid {:container true
              :justify :center
              :align-items :center}
     [ui/Grid {:item true
               :lg 8
               :md 10
               :xs 12}
      [ui/Paper {:class (<class styles :paper)}
       [:section.section>div.container>div.content
        [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}]]]]]))
