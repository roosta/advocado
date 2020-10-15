(ns advocado.form-validators
  (:require [debux.cs.core :refer-macros [clog]]
            [goog.object :as gobj]
            [cljsjs.zxcvbn]
            [tincture.core :refer [<sub]]))

(defn ?password-repeat
  "Validator using multiple inputs values.
   form - atom returned by form-init
   name - name of the input which call event"
  [form name]
  (let [password (get-in @form [:names->value :password])
        password-repeat (get-in @form [:names->value name])]
    (when-not (= password password-repeat)
      [:password-repeat :password-not-equal])))

(defn ?password-strength
  "Validator that checks the password strength."
  [form _]
  (let [password (get-in @form [:names->value :password])
        score (gobj/get (js/zxcvbn password) "score")]
    (when (or (= score 0)
              (= score 1))
      [:password-weak])))

(defn ?email-taken
  "Validator that checks signup data returned from server, and reports error if found."
  [_ _]
  (let [data (<sub [:signup.state/data])]
    (when (and (seq data) (contains? data "email"))
      [:email-taken])))

(defn ?username-taken
  "Validator that checks signup data returned from server, and reports error if found."
  [_ _]
  (let [data (<sub [:signup.state/data])]
    (when (and (seq data) (contains? data "username"))
      [:username-taken])))
