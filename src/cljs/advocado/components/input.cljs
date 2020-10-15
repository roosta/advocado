(ns advocado.components.input
  (:require [debux.cs.core :refer-macros [clog]]
            [form-validator.core :as fv]
            [advocado.color :as color]
            [goog.object :as gobj]
            [reagent.core :as r]
            [herb.core :refer [<class]]
            [advocado.styles.spacing :as spacing]
            [advocado.mui :as ui]))

; Input: {{{

(defn Input [form spec->msg {:keys [name on-change helper-text]
                             :or {helper-text " "}
                             :as params}]
  [ui/TextField (merge
                  {:class (<class spacing/margin 15 #{:bottom})
                   :full-width true
                   :on-change (if on-change on-change (partial fv/event->names->value! form))
                   :on-blur (partial fv/event->show-message form)
                   :error (fv/?show-message form name)
                   :helper-text (or (fv/?show-message form name spec->msg) helper-text)}
                  params)])

; }}}
; Select: {{{

(defn Select [form spec->msg {:keys [name label] :as params} & options]
  [ui/FormControl {:error (fv/?show-message form name)
                   :class (<class spacing/margin 15 #{:bottom})
                   :full-width true}
   [ui/InputLabel label]
   (into [ui/Select
          (merge {:value (get-in @form [:names->value name])
                  :on-change (partial (juxt fv/event->names->value! fv/event->show-message) form)}
                 (dissoc params :label))]
         options)
   [ui/FormHelperText (fv/?show-message form name spec->msg)]])

; }}}
; InputPassword: {{{

(defn bar-color [password-strength color-strength]
  (cond
    (and (>= password-strength 0)
         (< password-strength 50)) (color/color :red color-strength)

    (and (>= password-strength 50)
         (< password-strength 100)) (color/color :blue color-strength)

    (= password-strength 100) (color/color :green color-strength)))

(defn progress-bar-style [password-strength]
  {:background-color (bar-color password-strength 500)})

(defn progress-root-style [password-strength]
  {:background-color (bar-color password-strength 100)})

(defn InputPassword [form spec->msg params]
  (let [password-strength (r/atom -1)]
    (fn []
      [ui/FormControl {:error (fv/?show-message form (:name params))
                       :class (<class spacing/margin 15 #{:bottom})
                       :full-width true}
       [ui/InputLabel {:htmlFor "input-password"}
        (:label params)]
       [ui/Input {:type (:type params)
                  :name (:name params)
                  :style {:border 0}
                  :disable-underline true
                  :on-change (fn [e]
                               (let [value (-> e .-target .-value)
                                     score (gobj/get (js/zxcvbn value) "score")]
                                 (reset! password-strength (* (/ score 4) 100))
                                 (fv/event->names->value! form e)))
                  :aria-describedby "input-helper-text"
                  :on-blur (partial fv/event->show-message form)
                  :id "input-password"}]
       [ui/LinearProgress {:variant :determinate
                           :class (<class progress-root-style @password-strength)
                           :classes {:bar1Determinate (<class progress-bar-style @password-strength)}
                           :value @password-strength}]
       [ui/FormHelperText {:id "input-helper-text"}
        (or (fv/?show-message form (:name params) spec->msg) " ")]
       ])))

; }}}
; Checkbox: {{{

(defn error-style []
  {:color (color/color :red 500)})

(defn Checkbox [form spec->msg {:keys [name label] :as params}]
  [ui/FormControl {:error (fv/?show-message form name)
                   :class (<class spacing/margin 15 #{:bottom})}
   [ui/FormControlLabel
    {:control (r/as-element
                [ui/Checkbox
                 (merge
                   {:on-change (partial (juxt fv/event->names->value! fv/event->show-message) form)
                    :color :primary
                    :class (when (fv/?show-message form name) (<class error-style))}
                   (dissoc params :label))])
     :label label}]
   [ui/FormHelperText
    (fv/?show-message form (:name params) spec->msg)
    ]])

; }}}
; FormGroup: {{{

(defn FormGroup [form spec->msg {:keys [name label] :as params} & options]
  [ui/FormControl {:error (fv/?show-message form name)
                   :class (<class spacing/margin 15 #{:bottom})
                   :on-change (fn [e]
                                (let [value (-> e .-target .-value)
                                      current (get-in @form [:names->value name])
                                      f (if (contains? current value) disj conj)]
                                  (swap! form update-in [:names->value name] f value)
                                  (fv/validate-form form)))
                   :component "fieldset"}
   [ui/FormLabel label]
   (into
     [ui/FormGroup {:row true}]
     options)
   [ui/FormHelperText
    (fv/?show-message form (:name params) spec->msg)]])

; }}}
