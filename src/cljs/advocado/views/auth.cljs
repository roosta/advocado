(ns advocado.views.auth
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [debux.cs.core :refer-macros [clog]]
            [advocado.styles.spacing :as spacing]
            [advocado.form-validators :refer [?password-repeat ?password-strength ?email-taken ?username-taken]]
            [form-validator.core :as fv]
            [goog.object :as gobj]
            [tincture.core :refer [<sub >evt]]
            [advocado.components.input :refer [Input InputPassword Checkbox FormGroup]]
            [herb.core :refer [<class defgroup]]
            [garden.units :refer [px]]
            [advocado.validation :as v]
            [advocado.mui :as ui]))

; Wrapper component: {{{

(def flex #{"-webkit-flex" "-ms-flexbox" "flex"})

(defn wrapper-paper-style [height]
  {:padding "16px"
   :height (px height)
   :margin "16px"})

(declare wrapper-styles)
(defgroup wrapper-styles
  {:root {:display flex
          :min-height "inherit"}
   :child-container {:height "100%"}
   :logo {:max-width "100%"
          :margin [[(px 24) 0 (px 24) 0]]}})

(defn Wrapper
  [{:keys [height]}]
  [ui/Grid {:container true
            :justify "center"
            :align-items "center"
            :spacing 0
            :class (<class wrapper-styles :root)}
   [ui/Grid {:component (gobj/get js/MaterialUI "Paper")
             :class (<class wrapper-paper-style height)
             :item true
             :xs 12
             :sm 10
             :md 8
             :lg 6}
    [ui/Grid {:container true
              :class (<class wrapper-styles :child-container)
              :align-items "center"
              :justify "center"}
     (into [ui/Grid {:item true
                     :sm 8
                     :md 6
                     :xs 12}
            #_[:img {:class (<class wrapper-styles :logo)
                   :src "svg/logo.svg"}]]
           (r/children (r/current-component)))]]])



;}}}
; Login: {{{
(defn LoginForm []
  (let [form-conf {:names->value {:email ""
                                  :password ""}
                   :form-spec :auth/login-form}
        form (fv/init-form form-conf)
        spec->msg {::v/email "Please type a valid email."
                   ::v/password-length "Password has to be minimum 6 characters."
                   ::v/password-not-empty "Password can't be empty."} ]
    (fn []
      (let [state (<sub [:login/state])]
        [ui/Grid {:container true}
         [ui/Grid {:item true
                   :xs 12
                   :component :form
                   :on-submit (fn [e]
                                (when (fv/validate-form-and-show? form)
                                  (let [email (get-in @form [:names->value :email])
                                        password (get-in @form [:names->value :password])]
                                    (rf/dispatch [:login/submit {:email email :password password}])
                                    (js/console.log "on submit" email)))
                                (.preventDefault e))}
          [ui/Typography {:color :primary
                          :variant :h5
                          :class (<class spacing/margin 24 #{:bottom})}
           "Login to Advocado"]
          (when state
            [ui/Typography {:variant :body1
                            :class (<class spacing/margin 15 #{:bottom})
                            :color :error}
             state])
          [Input form spec->msg {:label "Email" :name :email}]
          [Input form spec->msg {:label "Password" :name :password :type :password}]
          [ui/Button {:color "primary"
                      :variant "contained"
                      :full-width true
                      :class (<class spacing/margin 15 #{:bottom})
                      :type "submit"}
           "Login"]
          [ui/Grid {:container true
                    :justify :flex-end}
           [ui/Typography {:color "primary"}
            [ui/Link {:href "/#/forgot-password"}
             "Forgot password"]]]]]))))

(defn Login []
  (r/create-class
    {:component-will-unmount #(rf/dispatch [:login.state/set nil])
     :reagent-render
     (fn []
       [Wrapper {:height 400}
        [LoginForm]])}))

;}}}
; ForgotPass: {{{

(defn ForgotPassForm []
  (let [form-conf {:names->value {:email ""}
                   :form-spec :auth/forgot-pass-form}
        form (fv/init-form form-conf)
        spec->msg {::v/email "Please type a valid email."}]
    (fn []
      (let [state (<sub [:forgot-password/state])
            [ok? message] state]
        [ui/Grid {:container true}
         [ui/Grid {:item true
                   :xs 12
                   :on-submit (fn [e]
                                (when (fv/validate-form-and-show? form)
                                  (let [email (get-in @form [:names->value :email])]
                                    (rf/dispatch [:forgot-password/submit {:email email}])))
                                (.preventDefault e))
                   :component :form}
          [ui/Typography {:color :primary
                          :variant :h5
                          :class (<class spacing/margin 24 #{:bottom})}
           "Forgot password?"]
          [ui/Typography {:color :textSecondary
                          :class (<class spacing/margin 15 #{:bottom})}
           "Enter the email address you used when you joined and we’ll send you instructions to reset your password." ]
          [ui/Typography {:color :textSecondary
                          :class (<class spacing/margin 15 #{:bottom})}
           "For security reasons, we do NOT store your password. So rest assured that we will never send your password via email."]
          (when state
            [ui/Typography {:variant :body1
                            :class (<class spacing/margin 15 #{:bottom})
                            :color (if ok? :textPrimary :error)}
             message])
          [Input form spec->msg {:label "Email" :name :email :disabled ok?}]
          [ui/Button {:color "primary"
                      :variant "contained"
                      :full-width true
                      :disabled ok?
                      :class (<class spacing/margin 15 #{:bottom})
                      :type "submit"}
           "Request new password"]
          [ui/Grid {:container true
                    :justify :flex-end}
           [ui/Typography {:color "primary"}
            [ui/Link {:href "/#/"}
             "Cancel"]]]]]))))

(defn ForgotPass []
  (r/create-class
    {:component-will-unmount #(>evt [:forgot-password.state/set nil])
     :reagent-render
     (fn []
       [Wrapper {:height 500}
        [ForgotPassForm]])}))

;}}}
; ResetPass: {{{

(defn ResetPassForm []
  (let [form-conf {:names->value {:password ""
                                  :password-repeat ""}
                   :form-spec :auth/reset-pass-form
                   :names->validators {:password-repeat [?password-repeat]
                                       :password [?password-strength]}}
        form (fv/init-form form-conf)
        spec->msg {::v/password-length "Password has to be minimum 6 characters."
                   ::v/password-not-empty "Password can't be empty."
                   :password-weak "Password is weak."
                   :password-not-equal "Password has to be the same."}]

    (fn []
      (if (<sub [:auth/valid-path-token])
        (let [[ok? message] (<sub [:reset-password/state])]
          (cond
            (true? ok?) [ui/Grid {:container true}
                          [ui/Grid {:item true
                                    :xs 12}
                           [ui/Typography {:align :center
                                           :class (<class spacing/margin 24 #{:bottom})}
                            message]
                           [ui/Button {:color :primary
                                       :variant :contained
                                       :href "/#/"
                                       :full-width true
                                       :class (<class spacing/margin 15 #{:bottom})}

                            "Login"]]]
            (nil? ok?) [ui/Grid {:container true}
                        [ui/Grid {:item true
                                  :xs 12
                                  :on-submit (fn [e]
                                               (.preventDefault e)
                                               (when (fv/validate-form-and-show? form)
                                                 (let [token (:token (<sub [:nav/path-params]))
                                                       pass (get-in @form [:names->value :password]) ]
                                                   (>evt [:reset-password/submit {:token token
                                                                                  :password pass}]))))
                                  :component :form}
                         [ui/Typography {:color :primary
                                         :variant :h5
                                         :class (<class spacing/margin 24 #{:bottom})}
                          "Change your password"]
                         #_[ui/Typography {:color :textSecondary}
                          "This link is only valid for a short time after the email was dispatched."]
                         [InputPassword form spec->msg {:label "Password" :type :password :name :password}]
                         [Input form spec->msg {:label "Repeat password" :type :password :name :password-repeat}]
                         [ui/Button {:color :primary
                                     :variant :contained
                                     :full-width true
                                     :class (<class spacing/margin 15 #{:bottom})
                                     :type :submit}
                          "Change password"]
                         [ui/Grid {:container true
                                   :justify :flex-end}
                          [ui/Typography {:color "primary"}
                           [ui/Link {:href "/#/"}
                            "Cancel"]]]]]
            (false? ok?) [ui/Grid {:container true}
                          [ui/Grid {:item true
                                    :xs 12}
                           [ui/Typography {:align :center
                                           :color :error
                                           :class (<class spacing/margin 24 #{:bottom})
                                           :variant :h6}
                            "Error processing request:"]
                           [ui/Typography {:align :center
                                           :class (<class spacing/margin 24 #{:bottom})}
                            message]
                           [ui/Grid {:container true
                                     :justify :center}
                            [ui/Link {:href "/#/forgot-password"}
                             "Go back"]]
                           ]]))
        [ui/Grid {:container true}
         [ui/Grid {:item true
                   :xs 12}
          [ui/Typography {:align :center
                          :color :error}
           "Invalid or expired token. Please try submitting a reset request again, and remember the token is only valid for 10 minutes."]
          [ui/Grid {:container true
                    :justify :center}
           [ui/Link {:href "/#/forgot-password"
                     :class (<class spacing/margin 24 #{:top :bottom})}
            "Go back"]]]
         ]))))

(defn ResetPass []
  (r/create-class
    {:component-will-unmount #(>evt [:reset-password.state/set nil])
     :reagent-render
     (fn []
       [Wrapper {:height 400}
        [ResetPassForm]])}))

;}}}
; Signup: {{{

(defn SignupForm []
  (let [form-conf {:names->value {:password ""
                                  :password-repeat ""
                                  :roles #{}
                                  :email ""
                                  :agree false
                                  :username ""
                                  :first-name ""
                                  :last-name ""}
                   :form-spec :auth/signup-form
                   :names->validators {:password-repeat [?password-repeat]
                                       :password [?password-strength]
                                       :email [?email-taken]
                                       :username [?username-taken]}}
        form (fv/init-form form-conf)
        spec->msg {::v/password-length "Password has to be minimum 6 characters."
                   ::v/email "Please type a valid email."
                   ::v/roles "You must choose at least one account type"
                   ::v/username-not-empty "Username can't be empty"
                   ::v/username-min-length "Username cannot be shorter than 6 characters"
                   ::v/username-max-length "Username cannot be longer than 30 characters"
                   ::v/username-valid "Username contains invalid character(s)"
                   ::v/first-name "First name can't be empty"
                   ::v/last-name "Last name can't be empty"
                   ::v/name-length "Name is to long"
                   ::v/password-not-empty "Password can't be empty"
                   ::v/agree "You need to agree before you can proceed"
                   :password-weak "Password is weak."
                   :username-taken "Username has already been taken"
                   :email-taken "Email has already been taken"
                   :password-not-equal "Password has to be the same"}]
    (r/create-class
      {:component-did-update #(>evt [:signup.state.data/reset])
       :reagent-render
       (fn []
         (let [msg (<sub [:signup.state/message])]
           (when (seq (<sub [:signup.state/data]))
             (fv/validate-form-and-show? form))
           [ui/Grid {:container true}
            [ui/Grid {:item true
                      :xs 12
                      :on-submit (fn [e]
                                   (.preventDefault e)
                                   (when (fv/validate-form-and-show? form)
                                     (let [email (get-in @form [:names->value :email])]
                                       (rf/dispatch [:signup/submit (dissoc (get @form :names->value) :password-repeat)])
                                       (js/console.log "on signup submit: " email))))
                      :component :form}
             [ui/Typography {:color :primary
                             :variant :h5
                             :class (<class spacing/margin 24 #{:bottom})}
              "Sign up to Advocado"]
             (when msg
               [ui/Typography {:variant :body1
                               :class (<class spacing/margin 15 #{:bottom})
                               :color :error}
                msg])
             [ui/Grid {:container true
                       :spacing 3}
              [ui/Grid {:item true
                        :xs 6}
               [Input form spec->msg {:label "First Name" :name :first-name}]]
              [ui/Grid {:item true
                        :xs 6}
               [Input form spec->msg {:label "Last Name" :name :last-name}]]]
             [Input form spec->msg
              {:label "Username"
               :name :username
               :on-change (fn [e]
                            (>evt [:signup.state.data/remove "username"])
                            (fv/event->names->value! form e))}]
             [Input form spec->msg
              {:label "Email"
               :name :email
               :on-change (fn [e]
                            (>evt [:signup.state.data/remove "email"])
                            (fv/event->names->value! form e))}]
             [InputPassword form spec->msg {:label "Password" :type :password :name :password}]
             [Input form spec->msg {:label "Repeat password" :type :password :name :password-repeat}]
             [FormGroup form spec->msg {:label "Choose your account types" :name :roles}
              [ui/FormControlLabel
               {:control (r/as-element [ui/Checkbox {:color :primary
                                                     :value :buyer}])
                :label "Buyer"}]
              [ui/FormControlLabel
               {:control (r/as-element [ui/Checkbox {:color :primary
                                                     :value :seller}])
                :label "Seller"}]]
             #_[Select form spec->msg {:label "Account type" :name :roles}
                [ui/MenuItem {:value "seller"} "Seller"]
                [ui/MenuItem {:value "buyer"} "Buyer"]
                [ui/MenuItem {:value "both"} "Both"]]
             [Checkbox form spec->msg
              {:label
               (r/as-element
                 [ui/Typography
                  "Creating an account means you're okay with our "
                  [ui/Link {:href "/#/terms"
                            :target "_blank"}
                   "Terms of Service"]
                  " and our "
                  [ui/Link {:href "/#/privacy"
                            :target "_blank"}
                   "Privacy Policy"]
                  "."
                  ])
               :name :agree}]
             [ui/Button {:color :primary
                         :variant :contained
                         :full-width true
                         :class (<class spacing/margin 15 #{:bottom})
                         :type :submit}
              "Create account"]
             [ui/Grid {:container true
                       :justify :flex-end}
              [ui/Typography {:color "primary"}
               [ui/Link {:href "/#/"}
                "Cancel"]]]]]))})))

(defn Signup []
  (r/create-class
    {:component-will-unmount #(>evt [:reset-password.state/set nil])
     :reagent-render
     (fn []
       [Wrapper {:height 825}
        [SignupForm]])}))

;}}}
