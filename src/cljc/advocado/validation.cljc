(ns advocado.validation
  (:require [struct.core :as st]
            [clojure.spec.alpha :as s]))

; Re-frame {{{

(defn deep-merge
  "For recursively merging deeply-nested maps"
  [v & vs]
  (letfn [(rec-merge [v1 v2]
            (if (and (map? v1) (map? v2))
              (merge-with deep-merge v1 v2)
              v2))]
    (if (some identity vs)
      (reduce #(rec-merge %1 %2) v vs)
      (last vs))))

(def default-db {:login/state nil
                 :docs {:home nil}
                 :auth/current-token nil
                 :forgot-password/state nil
                 :reset-password/state nil
                 :nav/loading false})

(defn reset-db [db]
  (deep-merge db default-db))

(s/def :login/state (s/nilable string?))
(s/def :forgot-password/state (s/nilable (s/cat :ok? boolean? :message string?)))
(s/def :reset-password/state (s/nilable (s/cat :ok? boolean? :message string?)))
(s/def :nav/loading boolean?)

(s/def ::db (s/keys :req [:login/state
                          :forgot-password/state
                          :reset-password/state
                          :nav/loading]))

; }}}
; Miscellaneous {{{

(s/def ::email (s/and string? (partial re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")))
(s/def ::token string?)
(s/def ::exp int?)
(s/def ::roles (s/and (s/coll-of string?) not-empty))
(s/def ::agree (s/and boolean? true?))

; }}}
; Password {{{

(s/def ::password-not-empty not-empty)
(s/def ::password-length #(<= 6 (count %)))
(s/def ::password (s/and string? ::password-not-empty ::password-length))

; }}}
; Name {{{

(s/def ::name-length #(>= 35 (count %)))
(s/def ::last-name (s/and string? not-empty ::name-length))
(s/def ::first-name (s/and string? not-empty ::name-length))

; }}}
; Username {{{

(s/def ::username-min-length #(<= 6 (count %)))
(s/def ::username-max-length #(>= 30 (count %)))
(s/def ::username-not-empty not-empty)
(s/def ::username-valid (partial re-matches #"^(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$"))
(s/def ::username (s/and string? ::username-not-empty ::username-valid ::username-min-length ::username-max-length))

; }}}
; Forms {{{

(s/def :auth/login-form (s/keys :req-un [::email ::password]))
(s/def :auth/forgot-pass-form (s/keys :req-un [::email]))
(s/def :auth/reset-pass-form (s/keys :req-un [::password]))
(s/def :auth/reset-pass-request (s/keys :req-un [::password ::token]))
(s/def :auth/token (s/keys :req-un [::email ::exp]))
(s/def :auth/signup-form (s/keys :req-un [::first-name ::last-name ::username ::email ::password ::roles ::agree]))
(s/def :account/settings-form (s/keys :req-un [::username ::email]))

; }}}
