(ns partnorize-api.external-api.salesforce
  (:require [clj-http.client :as http]
            [honey.sql.helpers :as h]
            [lambdaisland.uri :as uri]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(def client-id "3MVG9HB6vm3GZZR_toYEItEdxC3yoPsewXbClCDa2GZ9fi6mBqsAG2GNlVVj17possa3.lBE08Y88uBz4HkAH")
(def client-secret "479AD063C715B71A2EC24545310C11CED013D13A1B37FA0B9141D652495983BA")
(def redirect-uri "https://c1c0-2603-6010-5307-c1c3-f081-d3fc-8ae9-4928.ngrok-free.app/v0.1/auth/salesforce")

(defn generate-salesforce-login-link [state]
  (-> (uri/uri "https://login.salesforce.com/services/oauth2/authorize")
      (assoc :query (uri/map->query-string {:client_id client-id
                                            :redirect_uri redirect-uri
                                            :response_type "code"
                                            :state state}))
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
        :body)
    (catch Exception e
      (println "get-sf-access-token exception" e)
      nil)))

;; TODO paging
;; TODO filter by user
(defn query-opportunities
  ([instance-url access-token] (query-opportunities instance-url access-token nil))
  ([instance-url access-token company-name]
   (let [{} ()
         query (db/->format
                (cond-> (-> (h/select :id :name :amount :account.id :account.name)
                            (h/from :opportunity)
                            (h/limit 25)
                            (h/order-by :LastModifiedDate))
                  company-name (h/where [:like :name (str "%" company-name "%")])))
         response (http/get (u/make-link instance-url "/services/data/v59.0/query")
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
  (query-opportunities "https://thebuyersphere-dev-ed.develop.my.salesforce.com" "00DHs000002k3xp!AQcAQDbb_VNa_nRy4IonODB5TqI3NaZT7criTMkdPaLfxeCY.B0ZH5UECsO5oacdKk6MXJw6L669wp.1TWnJ5.UT3T5LnXG5")
  ;
  )