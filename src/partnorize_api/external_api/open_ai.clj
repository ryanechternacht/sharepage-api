(ns partnorize-api.external-api.open-ai
  (:require [cheshire.core :as json]
            [cljstache.core :as stache]
            [clj-http.client :as http]
            [partnorize-api.middleware.config :as config]))

(defn generate-message 
  ([ai-config prompt]
   (generate-message ai-config prompt nil))
  ([{:keys [api-key]} prompt context]
  (let [body {:model "gpt-4o-mini"
              :messages [
                         {:role "system"
                          :content (or context "You are a helpful assistant. Please ignore html in the prompts. Please generate your response in html")}
                         {:role "user"
                          :content prompt}]}
        response (http/post "https://api.openai.com/v1/chat/completions"
                            {:content-type :json
                             :as :json
                             :accept :json
                             :oauth-token api-key
                             :body (json/generate-string body)})]
    (-> response :body :choices first :message :content))))

(comment
  (generate-message (:open-ai config/config) "Write 2-3 sentences introducing myself in a friendly tone to someone named Tom Singell. We are connected because we both love Pickleball")
  ;
  )

(comment

  (let [prompt "Write top 10 quotes reflecting what my persona is most likely to think when they experience pains and frustrations when trying to get their job done.
             
             Now pick out the top 3 most likely given their persona. Write each with a 3 to 7 word header followed by one or two sentences that summarize. 
                    
                    Only provide the answer, no other context. Use html formatting, but only <ul>, <li>, <b>, and <i> tags"
        context {:buyer-name "Chad Spain"
                 :buyer-job-title "Director of Growth"
                 :buyer-account "Zello"
                 :buyer-location "Austin, Texas"
                 :buyer-website "crossbeam.com"
                 :seller-name "Archer"
                 :seller-job-title "Account Executive"
                 :seller-company "Sharepage"
                 :seller-website "https://www.scratchpad.com"}
        context-prompt (slurp "resources/ai/context-prompt.mustache")
        rendered-context (stache/render context-prompt context)]
    (generate-message (:open-ai config/config) prompt rendered-context))
  ;
  )
