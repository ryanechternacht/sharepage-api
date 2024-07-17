(ns partnorize-api.data.buyer-session
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn- start-session
  "starts a buyer session and returns the id for later queries to use"
  [db organization-id user-id swaypage-type swaypage-id virtual-swaypage-id
   {:keys [linked-name anonymous-id]}]
  (let [query (-> (h/insert-into :buyer_session)
                  (h/values [{:organization_id organization-id
                              :buyersphere_id swaypage-id
                              :user_account_id user-id
                              :linked_name linked-name
                              :anonymous_id anonymous-id
                              :swaypage_type swaypage-type
                              :virtual_swaypage_id virtual-swaypage-id}])
                  (h/returning :id))]
    (->> query
         (db/->>execute db)
         first)))

(defn start-swaypage-session [db organization-id user-id swaypage-id anonymous-user]
  (start-session db organization-id user-id "swaypage" swaypage-id nil anonymous-user))

(defn start-virtual-swaypage-session [db organization-id user-id virtual-swaypage-id anonymous-user]
  (start-session db organization-id user-id "virtual-swaypage" nil virtual-swaypage-id anonymous-user))


(comment
  (start-swaypage-session db/local-db 1 2 3 {:linked-name 4 :anonymous-id 5})
  (start-virtual-swaypage-session db/local-db 1 2 3 {:linked-name 4 :anonymous-id 5})
  ;
  )

(defn- track-time [db organization-id session-id swaypage-type buyersphere-id virtual-swaypage-id body]
  (let [insert-vals (->> body
                         (filter #(-> % :page-name parse-long))
                         (map (fn [{:keys [page-name time-on-page]}]
                                {:organization_id organization-id
                                 :buyersphere_id buyersphere-id
                                 :buyer_session_id session-id
                                 :page (parse-long page-name)
                                 :time_on_page (-> time-on-page Math/ceil long)
                                 :swaypage_type swaypage-type
                                 :virtual_swaypage_id virtual-swaypage-id})))
        query (-> (h/insert-into :buyer_session_timing)
                  (h/values insert-vals)
                  (h/on-conflict :buyer_session_id :page)
                  (h/do-update-set {:time_on_page :excluded.time_on_page}))]
    (db/execute db query)))

(defn track-swaypage-time [db organization-id session-id swaypage-id time-data]
  (track-time db organization-id session-id "swaypage" swaypage-id nil time-data))


(defn track-virtual-swaypage-time [db organization-id session-id virtual-swaypage-id time-data]
  (track-time db organization-id session-id "virtual-swaypage" nil virtual-swaypage-id time-data))

(comment
  (track-swaypage-time db/local-db 1 287 1 [{:page-name "2" :time-on-page 5}])
  (track-virtual-swaypage-time db/local-db 1 287 1 [{:page-name "5" :time-on-page 6}
                                                    {:page-name "6" :time-on-page 16}
                                                    {:page-name "asdf" :time-on-page 15}])

  ;
  )

(defn- track-event [db organization-id session-id swaypage-type swaypage-id virtual-swaypage-id page-name {:keys [event-type event-data]}]
  (when-let [page (parse-long page-name)]
    (let [query (-> (h/insert-into :buyer_session_event)
                    (h/values [{:organization_id organization-id
                                :buyersphere_id swaypage-id
                                :buyer_session_id session-id
                                :page page
                                :event_type event-type
                                :event_data [:lift event-data]
                                :swaypage_type swaypage-type
                                :virtual_swaypage_id virtual-swaypage-id}]))]
      (db/execute db query))))

(defn track-swaypage-event [db organization-id session-id swaypage-id page-name event-data]
  (track-event db organization-id session-id "swaypage" swaypage-id nil page-name event-data))

(defn track-virtual-swaypage-event [db organization-id session-id virtual-swaypage-id page-name event-data]
  (track-event db organization-id session-id "virtual-swaypage" nil virtual-swaypage-id page-name event-data))

(comment
  (track-swaypage-event db/local-db 1 287 94 "149" {:event-type "click-link"
                                                    :event-data {:link-text "i clicked it!"}})
  (track-virtual-swaypage-event db/local-db 1 289 94 "149" {:event-type "click-link"
                                                            :event-data {:link-text "i clicked it!"}})
  
  ;
  )

;; TODO redo these queries for paging (based on session)
(defn- get-time-tracking-base-query [organization-id]
  (-> (h/select :buyer_session.organization_id
                :buyer_session.buyersphere_id
                :buyer_session.virtual_swaypage_id
                :buyer_session.swaypage_type
                :buyer_session.id
                :buyer_session.virtual_swaypage_id
                :buyer_session.linked_name
                :buyer_session.anonymous_id
                :buyer_session.created_at
                :buyer_session_timing.page
                :buyer_session_timing.time_on_page
                :user_account.email
                :user_account.first_name
                :user_account.last_name
                :buyersphere.buyer
                :buyersphere.buyer_logo
                :buyersphere.priority
                :buyersphere_page.title
                :buyersphere_page.page_type
                :virtual_swaypage.page_data)
      (h/from :buyer_session)
      (h/left-join :buyersphere
              [:and
               [:= :buyer_session.organization_id :buyersphere.organization_id]
               [:= :buyer_session.buyersphere_id :buyersphere.id]])
      (h/join :buyer_session_timing
              [:and
               [:= :buyer_session.organization_id :buyer_session_timing.organization_id]
               [:= :buyer_session.id :buyer_session_timing.buyer_session_id]])
      (h/left-join :user_account
                   [:and
                    [:= :buyer_session.organization_id :user_account.organization_id]
                    [:= :buyer_session.user_account_id :user_account.id]])
      (h/join :buyersphere_page
              [:and
               [:= :buyer_session_timing.organization_id :buyersphere_page.organization_id]
               [:= [:cast :buyer_session_timing.page :int] :buyersphere_page.id]])
      (h/left-join :virtual_swaypage
                   [:and
                    [:= :buyer_session.organization_id :virtual_swaypage.organization_id]
                    [:= :buyer_session.virtual_swaypage_id :virtual_swaypage.id]])
      (h/where [:= :buyer_session.organization_id organization-id]
              ;;  remove really short sessions
               [:>= :buyer_session_timing.time_on_page 2])
      (h/order-by [:buyer_session.created_at :desc])))

(defn- get-event-tracking-base-query [organization-id]
  (-> (h/select :buyer_session.organization_id
                :buyer_session.buyersphere_id
                :buyer_session.id
                :buyer_session.linked_name
                :buyer_session.anonymous_id
                :buyer_session.created_at
                :buyer_session.virtual_swaypage_id
                :buyer_session_event.page
                :buyer_session_event.event_type
                :buyer_session_event.event_data
                :buyersphere.buyer
                :buyersphere.buyer_logo
                :buyersphere.priority
                :user_account.email
                :user_account.first_name
                :user_account.last_name
                :buyersphere_page.title
                :buyersphere_page.page_type
                :virtual_swaypage.page_data)
      (h/from :buyer_session)
      (h/left-join :buyersphere
                   [:and
                    [:= :buyer_session.organization_id :buyersphere.organization_id]
                    [:= :buyer_session.buyersphere_id :buyersphere.id]])
      (h/join :buyer_session_event
              [:and
               [:= :buyer_session.organization_id :buyer_session_event.organization_id]
               [:= :buyer_session.id :buyer_session_event.buyer_session_id]])
      (h/left-join :user_account
                   [:and
                    [:= :buyer_session.organization_id :user_account.organization_id]
                    [:= :buyer_session.user_account_id :user_account.id]])
      (h/join :buyersphere_page
              [:and
               [:= :buyer_session_event.organization_id :buyersphere_page.organization_id]
               [:= [:cast :buyer_session_event.page :int] :buyersphere_page.id]])
      (h/left-join :virtual_swaypage
                   [:and
                    [:= :buyer_session.organization_id :virtual_swaypage.organization_id]
                    [:= :buyer_session.virtual_swaypage_id :virtual_swaypage.id]])
      (h/where [:= :buyer_session.organization_id organization-id])
      (h/order-by [:buyer_session.created_at :desc])))

(defn format-entry [event]
  (-> event
      (dissoc :anonymous_id)
      (dissoc :first_name)
      (dissoc :last_name)
      (dissoc :email)
      (dissoc :linked_name)
      (dissoc :organization_id)
      (dissoc :buyersphere_id)
      (dissoc :buyer)
      (dissoc :buyer_logo)
      (dissoc :priority)
      (dissoc :created_at)
      (dissoc :page_data)
      (dissoc :virtual_swaypage_id)))

(defn group-and-format-data [timings events]
  (let [grouped-timings (group-by :id timings)
        grouped-events (group-by :id events)
        grouped
        (reduce (fn [acc [id es]]
                  (let [{:keys [linked_name first_name last_name
                                email anonymous_id buyersphere_id
                                organization_id created_at buyer
                                buyer_logo priority page_data virtual_swaypage_id]}
                        (first es)
                        buyersphere_data (if buyersphere_id
                                           {:id buyersphere_id
                                            :buyer buyer
                                            :buyer-logo buyer_logo
                                            :priority priority}
                                           {:id virtual_swaypage_id
                                            :buyer (:account-name page_data)
                                            :buyer-logo (:buyer-logo page_data)
                                            :priority 2})]
                    (conj acc {:linked-name linked_name
                               :first-name first_name
                               :last-name last_name
                               :email email
                               :anonymous-id anonymous_id
                               :buyersphere buyersphere_data
                               :organization-id organization_id
                               :created-at created_at
                               :timings (map format-entry es)
                               :events (map format-entry
                                            (get grouped-events id))})))
                []
                grouped-timings)]
    (->> grouped
         (sort-by :created-at)
         reverse)))

(defn get-buyer-sessions [db organization-id]
  (let [timings (db/execute db (get-time-tracking-base-query organization-id))
        events (db/execute db (get-event-tracking-base-query organization-id))]
    (group-and-format-data timings events)))

(defn get-buyer-sessions-for-swaypage [db organization-id buyersphere-id]
  (let [timings-query (-> (get-time-tracking-base-query organization-id)
                          (h/where [:= :buyer_session.buyersphere_id buyersphere-id]
                                   [:= :buyer_session.swaypage_type "swaypage"]))
        timings (db/execute db timings-query)
        events-query (-> (get-event-tracking-base-query organization-id)
                         (h/where [:= :buyer_session.buyersphere_id buyersphere-id]
                                  [:= :buyer_session.swaypage_type "swaypage"]))
        events (db/execute db events-query)]
    (group-and-format-data timings events)))


(defn get-buyer-sessions-for-virtual-swaypage [db organization-id virtual-swaypage-id]
  (let [timings-query (-> (get-time-tracking-base-query organization-id)
                          (h/where [:= :buyer_session.virtual_swaypage_id virtual-swaypage-id]
                                   [:= :buyer_session.swaypage_type "virtual-swaypage"]))
        timings (db/execute db timings-query)
        events-query (-> (get-event-tracking-base-query organization-id)
                         (h/where [:= :buyer_session.virtual_swaypage_id virtual-swaypage-id]
                                  [:= :buyer_session.swaypage_type "virtual-swaypage"]))
        events (db/execute db events-query)]
    (group-and-format-data timings events)))

(comment
  (get-buyer-sessions db/local-db 1)
  (get-buyer-sessions-for-swaypage db/local-db 1 94)
  (get-buyer-sessions-for-virtual-swaypage db/local-db 1 3)
  ;
  )
