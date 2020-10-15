(ns advocado.email
  (:require [org.httpkit.client :as http]
            [clojure.string :as str]
            [cheshire.core :refer [generate-string]]
            [clojure.tools.logging :as log]
            [hiccup.core :refer [html]]
            [advocado.config :refer [env]]))

(defn send-message
  ([from recipients subject url]
   (let [response (http/request {:url (str (:mailgun-base-url env) "/messages")
                                 :method :post
                                 :basic-auth ["api" (:mailgun-api-key env)]
                                 :form-params {:from    from
                                               :to      recipients
                                               :subject subject
                                               :template "forgot-pass.html"
                                               :h:X-Mailgun-Variables (generate-string {:url url})
                                               }})]
     (if (= (:status @response) 200)
       (log/info (str "Reset password email sent to " recipients ", responded with status: " (:status @response)))
       (throw (ex-info "Failed to send email\n" @response)))))
  ([from recipients subject body attachment]
   (let [response (http/request {:url (str (:mailgun-base-url env) "/messages")
                                 :method :post
                                 :basic-auth ["api" (:mailgun-api-key env)]
                                 :multipart [{:name "from", :content from}
                                             {:name "to", :content recipients}
                                             {:name "subject", :content subject}
                                             {:name "html", :content body}
                                             {:name "Content/type" :content "application/pdf"}
                                             {:name "attachment", :content attachment :filename (.toString attachment)}]})]
     (if (= (:status @response) 200)
       (log/info (str "Reset password email sent to " recipients ", responded with status: " (:status @response)))
       (throw (ex-info "Failed to send email" @response))))))
