(ns partnorize-api.external-api.salesforce
  (:require [clj-http.client :as http]
            [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(def access-token "00DHs000002k3xp!AQcAQDQvzGa7pfBY1g1QcchDjrRNAmANfvt7Oq1f0jXkPBssE1qS1LSTQHrouh_M4nX_fv_AfwmIlDcEnKstmo4IpgBT_i.S")

;; curl --location 'https://thebuyersphere-dev-ed.develop.my.salesforce.com/services/data/v59.0/query?q=select%20name%20from%20account%20' \
;; --header 'Authorization: Bearer 00DHs000002k3xp!AQcAQPsEifb49EI1pp2WODpi1DqIg9fcrzGgqSQaQxur4MX6_3A7M42qQLYZrM_BXwuHKhpGJl43NayVWVPLUzstNPsxsk2A' \
;; --header 'Cookie: BrowserId=GPE8dXaDEe6YzTP1KAxcMg; CookieConsentPolicy=0:1; LSKey-c$CookieConsentPolicy=0:1'

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