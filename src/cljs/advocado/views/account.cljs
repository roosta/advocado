(ns advocado.views.account
  (:require [herb.core :refer [defgroup <class]]
            [tincture.core :refer [<sub]]
            [reagent.core :as r]
            [advocado.validation :as v]
            [form-validator.core :as fv]
            [advocado.components.link :refer [Link]]
            [advocado.components.input :refer [Input]]
            [debux.cs.core :refer [clog]]
            [garden.units :refer [px]]
            [advocado.mui :as ui]))

(def pages
  [[:account-profile "/#/account/profile" "Edit Profile"]
   [:account-settings "/#/account" "Account Settings"]
   [:account-password "/#/account/password" "Password"]])

; Menu component: {{{

(defn active-link-style
  []
  {:font-weight 500})

(defn MenuItem [comp-id route label]
  (let [page-id (<sub [:nav/page-id])]
    [ui/Grid {:item true
              :xs 12}
     [Link {:underline :none
            :color (if (= comp-id page-id) :textPrimary :textSecondary)
            :href route
            :class (when (= page-id comp-id) (<class active-link-style))}
      label]]))

(defn Menu []
  [ui/Grid {:container true
            :spacing 1}
   (for [[comp-id route label] pages]
     ^{:key comp-id}
     [MenuItem comp-id route label])])

; }}}
; Header component {{{

(defn header-style []
  {:margin-bottom (px 32)})

(defn Header [heading subtitle]
  (let [full-name (<sub [:user/full-name])
        username (<sub [:user/username])]
    [ui/Grid {:item true
              :class (<class header-style)
              :xs 12}
     [ui/Breadcrumbs
      [Link {:variant :h5
             :underline :none
             :color-direction :lighten
             :href (str "/#/profile/" username)}
       full-name]
      [ui/Typography {:variant :h5}
       heading]]
     [ui/Typography {:color :textSecondary}
      subtitle]]))

; }}}
; Wrapper component {{{

(defn paper-style []
  {:padding (px 32)})

(defn Wrapper []
  [ui/Grid {:container true
            :justify :center
            :align-items :center}
   [ui/Grid {:item true
             :lg 6
             :md 8
             :xs 12}
    [ui/Paper {:class (<class paper-style)}
     (into [ui/Grid {:container true}]
           (r/children (r/current-component)))]]])

; }}}
; Account view: {{{

(defn AccountForm []
  (let [form-conf {:names->value {:username ""
                                  :email ""}
                   :form-spec :account/settings-form}
        form (fv/init-form form-conf)
        spec->msg {::v/username-valid "Username contains invalid character(s)"
                   ::v/username-not-empty "Username can't be empty"
                   ::v/username-min-length "Username cannot be shorter than 6 characters"
                   ::v/username-max-length "Username cannot be longer than 30 characters"
                   :username-taken "Username has already been taken"
                   :email-taken "Email has already been taken"
                   ::v/email "Please type a valid email."}]
    (fn []
      (let [username (<sub [:user/username])
            email (<sub [:user/email])]
        [ui/Grid {:container true}
         [ui/Grid {:item true
                   :xs 10}
          [Input form spec->msg
           {:label "Username"
            :name :username
            :default-value username
            :helper-text (str "Your Advocado URL: https://advocado.com/profile/" username)}]
          [Input form spec->msg
           {:label "Email"
            :default-value email
            :name :email}]]]))))

(defn Account []
  [Wrapper
   [Header
    "Account Settings"
    "Update your username and manage your account"]
   [ui/Grid {:item true
             :xs 3}
    [Menu]]
   [ui/Grid {:item true
             :xs 9}
    [AccountForm]
    ]
   ])
;;}}}
; Password view: {{{

(defn Password []
  [Wrapper
   [Header
    "Password"
    "Manage your password"]
   [ui/Grid {:item true
             :xs 3}
    [Menu]
    ]])

; }}}
; Profile view: {{{

(defn Profile []
  [Wrapper
   [Header
    "Edit Profile"
    "Set up your Advocado profile"]
   [ui/Grid {:item true
             :xs 3}
    [Menu]
    ]])

; }}}
