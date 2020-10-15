(ns advocado.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf :refer [inject-cofx]]
    [advocado.components.appframe :refer [AppFrame]]
    [advocado.ajax :as ajax]
    [tincture.core :as tinc :refer [<sub >evt]]
    [advocado.validation :refer [reset-db]]
    [advocado.nav]
    [advocado.local-store]
    [advocado.auth]
    [advocado.theme :as theme]
    [advocado.docs :as docs]
    [advocado.interceptors :refer [check-spec]]
    [advocado.components.loader :refer [Loader]]
    [herb.core :refer [defglobal]]
    [cljsjs.material-ui]
    [form-validator.core :as form-validator]
    [advocado.color :as color]))

(rf/reg-event-fx
  ::initialize
  [check-spec (inject-cofx :local-store :token)]
  (fn [{:keys [db local-store]}]
    {:db (-> (reset-db db)
             (assoc :auth/current-token local-store))}))

(defglobal global-style
  [:html :body :#app {:height "100%"
                      :margin 0
                      :background-color (color/color :grey 100)
                      }])

(defn page []
  (when-let [page (<sub [:nav/page])]
    (let [loading? (<sub [:nav/loading])]
      [AppFrame
       (if loading?
         [Loader]
         [page])]
      )))


;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init-theme []
  (rf/dispatch
   [:theme/initialize
    {:typography {:useNextVariants true}
     :palette    {:type      "light"
                  :primary   {:light (color/color "teal" 300)
                              :main  (color/color "teal" 500)
                              :dark  (color/color "teal" 700)}
                  :secondary {:light (color/color "pink" 300)
                              :main  (color/color "pink" 500)
                              :dark  (color/color "pink" 700)}
                  :error     {:light (color/color "red" 300)
                              :main  (color/color "red" 500)
                              :dark  (color/color "red" 700)}}}])
  )

(defn init! []
  (>evt [::initialize])
  (>evt [:router/start])
  (ajax/load-interceptors!)
  (tinc/init!)
  (init-theme)

  ;; Init form validator
  (swap! form-validator/conf #(merge % {:atom r/atom}))

  ;; Render app
  (mount-components))

