(ns partnorize-api.external-api.salesforce
  (:require [clj-http.client :as http]
            [honey.sql.helpers :as h]
            [lambdaisland.uri :as uri]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.data.salesforce-access :as d-sf]
            [partnorize-api.middleware.config :as config]))

(defn generate-salesforce-login-link
  [{:keys [client-id redirect-uri]} state]
  (-> (uri/uri "https://login.salesforce.com/services/oauth2/authorize")
      (assoc :query (uri/map->query-string {:client_id client-id
                                            :redirect_uri redirect-uri
                                            :response_type "code"
                                            :state state}))
      str))

(defn get-sf-access-token [{:keys [client-id client-secret redirect-uri]} code]
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
      (throw e))))

(defn get-new-sf-access-token [{:keys [client-id client-secret]} refresh_token]
  (try
    (-> (http/post "https://login.salesforce.com/services/oauth2/token"
                   {:accept :json
                    :as :json
                    :form-params {:grant_type "refresh_token"
                                  :client_id client-id
                                  :client_secret client-secret
                                  :refresh_token refresh_token}})
        :body)
    (catch Exception e
      (println "get-sf-access-token exception" e)
      (throw e))))

(defn get-sf-user-info [access-token sf-identity-url]
  (try
    (-> (http/get sf-identity-url
                  {:accept :json
                   :as :json
                   :oauth-token access-token})
        :body)
    (catch Exception e
      (println "get-sf-user-info exception" e)
      (throw e))))

;; TODO paging
;; TODO filter by user
(defn query-opportunities
  ([instance-url access-token company-name owner]
   (let [query (db/->format
                (cond-> (-> (h/select :id :name :amount :account.id :account.name :owner.name :closedate :account.website)
                            (h/from :opportunity)
                            (h/where [:= :isclosed false])
                            (h/limit 50)
                            (h/order-by :LastModifiedDate))
                  company-name (h/where [:like :name (str "%" company-name "%")])
                  owner (h/where [:= :owner.id owner])))
         response (http/get (u/make-link instance-url "/services/data/v59.0/query")
                            {:oauth-token access-token
                             :accept :json
                             :as :json
                             :query-params {:q query}})]
     (->> response
          :body
          :records
          ;; TODO there are better ways to do this fo sho
          (map (fn [{id :Id name :Name amount :Amount close-date :CloseDate
                     {account-name :Name account-id :Id website :Website} :Account
                     {owner-name :Name} :Owner}]
                 {:id id
                  :name name
                  :amount amount
                  :account-name account-name
                  :account-id account-id
                  :owner-name owner-name
                  :close-date close-date
                  :logo (str "https://logo.clearbit.com/" (u/get-domain website))}))))))

;; TODO this sucks and should be refactored to suck less
(defn query-opportunities-with-sf-refresh!
  "calls query-opportunities, but if it fails will refresh the SF 
   access_token and try again"
  ([sf-config db organization-id user-id company-name only-mine?]
   (query-opportunities-with-sf-refresh! sf-config db organization-id user-id company-name only-mine? true))
  ([sf-config db organization-id user-id company-name only-mine? first-try?]
   (let [{:keys [instance_url access_token refresh_token sf_user_id]}
         (d-sf/get-salesforce-access-details db organization-id user-id)]
     (try 
       (query-opportunities instance_url access_token company-name (when only-mine? sf_user_id))
       (catch Exception ex
         (if first-try?
           (let [{new-access-token :access_token} (get-new-sf-access-token sf-config refresh_token)]
             (d-sf/save-salesforce-access-token! db organization-id user-id new-access-token)
             (query-opportunities-with-sf-refresh! sf-config db organization-id user-id company-name false))
           (do 
             (println "query opps with refresh error" ex)
             (throw ex))))))))

(comment
  (query-opportunities "https://thebuyersphere-dev-ed.develop.my.salesforce.com" "00DHs000002k3xp!AQcAQDbb_VNa_nRy4IonODB5TqI3NaZT7criTMkdPaLfxeCY.B0ZH5UECsO5oacdKk6MXJw6L669wp.1TWnJ5.UT3T5LnXG5" nil nil)
  (query-opportunities-with-sf-refresh! (:salesforce config/config) db/local-db 1 1 nil nil)
  (query-opportunities-with-sf-refresh! (:salesforce config/config) db/local-db 1 1 nil true)
  ;
  )
