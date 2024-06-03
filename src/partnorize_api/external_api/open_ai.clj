(ns partnorize-api.external-api.open-ai
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [partnorize-api.middleware.config :as config]))

(defn generate-message [{:keys [api-key]} prompt]
  (let [body {:model "gpt-3.5-turbo"
              :messages [;;  Maybe we will restore this later
                         ;;  {:role "system"
                         ;; :content "You are a helpful assistant. We are writing business introductions. Do not add a placeholder for my name. Ignore any html markup"}
                         {:role "user"
                          :content prompt}]}
        response (http/post "https://api.openai.com/v1/chat/completions"
                            {:content-type :json
                             :as :json
                             :accept :json
                             :oauth-token api-key
                             :body (json/generate-string body)})]
    (-> response :body :choices first :message :content)))

(comment
  (generate-message (:open-ai config/config) "Write 2-3 sentences introducing myself in a friendly tone to someone named Tom Singell. We are connected because we both love Pickleball")
  ;
  )
