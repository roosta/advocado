(ns advocado.views.privacy
  (:require [herb.core :refer [defgroup <class]]
            [advocado.mui :as ui]
            [tincture.core :refer [<sub]]
            [markdown.core :refer [md->html]]
            [re-frame.core :as rf]))

(defgroup styles
  {:paper {}})

(defn Privacy []
  (when-let [docs (<sub [:docs/privacy])]
    [ui/Grid {:container true
              :justify :center
              :align-items :center}
     [ui/Grid {:item true
               :lg 6
               :md 8
               :xs 12}
      [ui/Paper {:class (<class styles :paper)}
       [:section.section>div.container>div.content
        [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}]]]]]))
