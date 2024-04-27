(ns partnorize-api.data.buyer-session
  (:require [honey.sql.helpers :as h]
             [partnorize-api.data.buyerspheres :as buyersphere]
             [partnorize-api.data.users :as users]
             [partnorize-api.db :as db]))

(defn start-session 
  "starts a buyer session and returns the id for later queries to use"
  [db organization-id buyersphere-id user-id 
                     {:keys [linked-name anonymous-id]}]
  (let [query (-> (h/insert-into :buyer_session)
                  (h/values [{:organization_id organization-id
                              :buyersphere_id buyersphere-id
                              :user_account_id user-id
                              :linked_name linked-name
                              :anonymous_id anonymous-id}])
                  (h/returning :id))]
    (->> query
         (db/->>execute db)
         first)))

(comment
  (start-session db/local-db 1 2 3 {:linked-name 4 :anonymous-id 5})
  ;
  )



(defn track-time [db organization-id buyersphere-id session-id page time-on-page]
  (let [query (-> (h/insert-into :buyer_session_timing)
                  (h/values [{:organization_id organization-id
                              :buyersphere_id buyersphere-id
                              :buyer_session_id session-id
                              :page page
                              :time_on_page time-on-page}])
                  (h/on-conflict :buyer_session_id :page)
                  (h/do-update-set {:time_on_page [:+ :buyer_session_timing.time_on_page :excluded.time_on_page]}))]
    (db/execute db query)))

(comment
  (track-time db/local-db 1 1 1 "page3" 5)
  ;
  )
