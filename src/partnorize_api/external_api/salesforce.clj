(ns partnorize-api.external-api.salesforce
  (:require [clj-http.client :as http]
            [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [lambdaisland.uri :as uri]))

(def access-token "00DHs000002k3xp!AQcAQM8dq9LmGyD45Er28BBL_QZ7y3HYSeMLyg1UcR3XuEhj2yZpo8IWPc57olOwnM0HWoIuATb_EOwj4MaSjzyA6iKTEZC8")

(def client-id "3MVG9HB6vm3GZZR_toYEItEdxC3yoPsewXbClCDa2GZ9fi6mBqsAG2GNlVVj17possa3.lBE08Y88uBz4HkAH")
(def client-secret "479AD063C715B71A2EC24545310C11CED013D13A1B37FA0B9141D652495983BA")
(def redirect-uri "https://99c3-2603-6010-5307-c1c3-f081-d3fc-8ae9-4928.ngrok-free.app/v0.1/auth/salesforce")

(defn generate-salesforce-login-link []
  (-> (uri/uri "https://login.salesforce.com/services/oauth2/authorize")
      (assoc :query (uri/map->query-string {:client_id client-id
                                            :redirect_uri redirect-uri
                                            :response_type "code"}))
      str))

(defn get-sf-access-token [code]
  (try
    (-> (http/post "https://login.salesforce.com/services/oauth2/token"
                   {:accept :json
                    :as :json
                    :form-params {:grant_type "authorization_code"
                                  :code code
                                  :client_id client-id
                                  :client_secret client-secret
                                  :redirect_uri redirect-uri}})
        :body
        :access_token)
    (catch Exception e
      (println "get-sf-access-token exception" e)
      nil)))

;; TODO paging
(defn query-opportunities
  ([] (query-opportunities nil))
  ([company-name]
   (let [query (db/->format
                (cond-> (-> (h/select :id :name :amount :account.id :account.name)
                            (h/from :opportunity)
                            (h/limit 25)
                            (h/order-by :LastModifiedDate))
                  company-name (h/where [:like :name (str "%" company-name "%")])))
         response (http/get "https://thebuyersphere-dev-ed.develop.my.salesforce.com/services/data/v59.0/query"
                            {:oauth-token access-token
                             :accept :json
                             :as :json
                             :query-params {:q query}})]
     (->> response
          :body
          :records
          ;; TODO there are better ways to do this fo sho
          (map (fn [{id :Id name :Name amount :Amount
                     {account-name :Name account-id :Id} :Account}]
                 {:id id
                  :name name
                  :amount amount
                  :account-name account-name
                  :account-id account-id}))))))

(comment
  (query-opportunities)
  ;
  )