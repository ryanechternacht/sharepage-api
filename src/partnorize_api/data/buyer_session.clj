(ns partnorize-api.data.buyer-session
  (:require [honey.sql.helpers :as h]
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

(defn track-time [db organization-id buyersphere-id session-id body]
  (doseq [{:keys [page-name time-on-page]} body]
    (when-let [page (parse-long page-name)]
      (let [query (-> (h/insert-into :buyer_session_timing)
                      (h/values [{:organization_id organization-id
                                  :buyersphere_id buyersphere-id
                                  :buyer_session_id session-id
                                  :page page
                                  :time_on_page time-on-page}])
                      (h/on-conflict :buyer_session_id :page)
                      (h/do-update-set {:time_on_page :excluded.time_on_page}))]
        (db/execute db query)))))

(comment
  (track-time db/local-db 1 1 1 [{:page-name "page3" :time-on-page 5}])
  (track-time db/local-db 1 1 1 [{:page-name "3" :time-on-page 5}
                                 {:page-name "4" :time-on-page 10}])
  ;
  )

(defn- time-tracking-base-query [organization-id]
  (-> (h/select :buyer_session.organization_id
                :buyer_session.buyersphere_id
                :buyer_session.id
                :buyer_session.linked_name
                :buyer_session.anonymous_id
                :buyer_session.created_at
                :buyer_session_timing.page
                :buyer_session_timing.time_on_page
                :user_account.email
                :user_account.first_name
                :user_account.last_name
                :buyersphere_page.title
                :buyersphere_page.page_type)
      (h/from :buyer_session)
      (h/join :buyer_session_timing
              [:and
               [:= :buyer_session.organization_id :buyer_session_timing.organization_id]
               [:= :buyer_session.buyersphere_id :buyer_session_timing.buyersphere_id]
               [:= :buyer_session.id :buyer_session_timing.buyer_session_id]])
      (h/left-join :user_account
                   [:and
                    [:= :buyer_session.organization_id :user_account.organization_id]
                    [:= :buyer_session.user_account_id :user_account.id]])
      (h/join :buyersphere_page
              [:and
               [:= :buyer_session_timing.organization_id :buyersphere_page.organization_id]
               [:= :buyer_session_timing.buyersphere_id :buyersphere_page.buyersphere_id]
               [:= [:cast :buyer_session_timing.page :int] :buyersphere_page.id]])
      (h/where [:= :buyer_session.organization_id organization-id])
      (h/order-by [:buyer_session.created_at :desc])))

(defn format-event [event]
  (-> event
      (dissoc :anonymous_id)
      (dissoc :first_name)
      (dissoc :last_name)
      (dissoc :email)
      (dissoc :linked_name)
      (dissoc :organization_id)
      (dissoc :buyersphere_id)))

(defn format-events [events]
  (let [grouped (group-by :id events)]
    (reduce (fn [acc [_ es]]
              (let [{:keys [linked_name first_name last_name
                            email anonymous_id buyersphere_id
                            organization_id]} (first es)]
                (conj acc {:linked-name linked_name
                           :first-name first_name
                           :last-name last_name
                           :email email
                           :anonymous-id anonymous_id
                           :buyersphere-id buyersphere_id
                           :organization-id organization_id
                           :events (map format-event es)})))
            []
            grouped)))

(defn get-time-on-buyersphere [db organization-id buyersphere-id]
  (let [query (-> (time-tracking-base-query organization-id)
                  (h/where [:= :buyer_session.buyersphere_id buyersphere-id]))
        events (db/execute db query)]
    (format-events events)))

(comment
  (get-time-on-buyersphere db/local-db 1 94)
  ;
  )
