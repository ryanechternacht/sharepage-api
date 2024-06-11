(ns partnorize-api.data.buyerspheres
  (:require [clojure.instant :as inst]
            [honey.sql.helpers :as h]
            [nano-id.core :as nano]
            [partnorize-api.data.buyersphere-notes :as d-buyer-notes]
            [partnorize-api.data.buyersphere-pages :as d-buyer-pages]
            [partnorize-api.data.buyersphere-resources :as d-buyer-res]
            [partnorize-api.data.resources :as d-res]
            [partnorize-api.data.teams :as d-teams]
            [partnorize-api.data.buyersphere-activity-templates :as d-act-temp]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.db :as db]))

(def only-buyersphere-cols 
  [:buyersphere.id :buyersphere.organization_id :buyersphere.buyer
   :buyersphere.buyer_logo :buyersphere.intro_message
   :buyersphere.features_answer
   :buyersphere.pricing_can_pay :buyersphere.pricing_tier_id
   :buyersphere.current_stage :buyersphere.status
   :buyersphere.qualification_date :buyersphere.evaluation_date
   :buyersphere.decision_date :buyersphere.adoption_date
   :buyersphere.qualified_on :buyersphere.evaluated_on
   :buyersphere.decided_on :buyersphere.adopted_on
   :buyersphere.show_pricing :buyersphere.deal_amount
   :buyersphere.crm_opportunity_id :buyersphere.success_criteria_answer
   :buyersphere.objectives_answer :buyersphere.constraints_answer
   :buyersphere.subname :buyersphere.is_public
   :buyersphere.shortcode :buyersphere.room_type
   :buyersphere.priority :buyersphere.updated_at])

(def base-buyersphere-cols
  (vec (concat only-buyersphere-cols
               [[:user_account.id :owner_id] [:user_account.first_name :owner_first_name]
                [:user_account.last_name :owner_last_name] [:user_account.image :owner_image]
                [:buyer_session.created_at :most_recent_buyer_activity]])))

(defn- base-buyersphere-query [organization-id]
  (-> (apply h/select base-buyersphere-cols)
      (h/from :buyersphere)
      (h/left-join :user_account [:and
                                  [:= :buyersphere.organization_id :user_account.organization_id]
                                  [:= :buyersphere.owner_id :user_account.id]])
      (h/left-join [(-> (h/select :organization_id :buyersphere_id [:%max.created_at :created_at] )
                        (h/from :buyer_session)
                        (h/group-by :organization_id :buyersphere_id)) :buyer_session]
                   [:and
                    [:= :buyersphere.organization_id :buyer_session.organization_id]
                    [:= :buyersphere.id :buyer_session.buyersphere_id]])
      (h/where [:= :buyersphere.organization_id organization-id])
      (h/order-by [:updated_at :desc])))

(defn- format-buyersphere-owner
  [{:keys [owner_id owner_first_name owner_last_name owner_image] :as buyersphere}]
  (-> buyersphere
      (dissoc :owner_id)
      (dissoc :owner_first_name)
      (dissoc :owner_last_name)
      (dissoc :owner_image)
      (cond-> owner_id (assoc :owner {:id owner_id
                                      :first_name owner_first_name
                                      :last_name owner_last_name
                                      :image owner_image}))))

(defn get-by-ids [db organization-id ids]
  (let [buyerspheres (-> (base-buyersphere-query organization-id)
                         (h/where [:in :buyersphere.id ids])
                         (db/->execute db))]
    (->> buyerspheres
         (map #(update % :qualification_date u/to-date-string))
         (map #(update % :evaluation_date u/to-date-string))
         (map #(update % :decision_date u/to-date-string))
         (map format-buyersphere-owner))))

(defn get-by-id [db organization-id id]
  (first (get-by-ids db organization-id [id])))

;; TODO add status to buyersphers
(defn get-by-organization
  ([db organization-id]
   (get-by-organization db organization-id {}))
  ([db organization-id {:keys [user-id stage status]}]
   (let [query (cond-> (base-buyersphere-query organization-id)
                 (u/is-provided? user-id) (h/where [:in :buyersphere.id
                                                    (-> (h/select :buyersphere_id)
                                                        (h/from :buyersphere_user_account)
                                                        (h/where [:= :user_account_id user-id]))])
                 (u/is-provided? stage) (h/where [:= :buyersphere.current_stage stage])
                 (u/is-provided? status) (cond->
                                          (= status "not-active") (h/where [:<> :buyersphere.status "active"])
                                          (not= status "not-active") (h/where [:= :buyersphere.status status]))

                ;;  is-overdue (h/where [:or
                ;;                       [:and
                ;;                        [:= :current_stage "qualification"]
                ;;                        [:< :qualification_date [[:now]]]]
                ;;                       [:and
                ;;                        [:= :current_stage "evaluation"]
                ;;                        [:< :evaluation_date [[:now]]]]
                ;;                       [:and
                ;;                        [:= :current_stage "decision"]
                ;;                        [:< :decision_date [[:now]]]]])
                 true (h/order-by :buyersphere.buyer))]
      ;; TODO what to order on?
     (->> query 
          (db/->>execute db)
          (map format-buyersphere-owner)))))

;; TODO is this how I want this to work?
;; TODO do i still need this route?
(defn get-full-buyersphere [db organization-id id]
  (let [buyersphere (get-by-id db organization-id id)
        resources (d-buyer-res/get-buyersphere-resources-by-buyersphere-id db organization-id id)
        notes (d-buyer-notes/get-by-buyersphere db organization-id id)
        {:keys [buyer-team seller-team]} (d-teams/get-by-buyersphere db organization-id id)]
    (-> buyersphere
        (assoc :resources resources)
        (assoc :notes notes)
        (assoc :buyer_team buyer-team)
        (assoc :seller_team seller-team)
        (update :qualification_date u/to-date-string)
        (update :evaluation_date u/to-date-string)
        (update :decision_date u/to-date-string)
        format-buyersphere-owner)))

(defn get-by-user
  "This is intended for buyers to check which buyerspheres they 
   are part of so the UI can send them to the right place 
   (otherwise it's hard for them to get there w/o knowing the url)"
  [db organization-id user-id]
  (-> (base-buyersphere-query organization-id)
      (h/join :buyersphere_user_account [:= :buyersphere.id :buyersphere_user_account.buyersphere_id])
      (h/where [:= :buyersphere_user_account.user_account_id user-id])
      (db/->execute db)))

(defn get-by-opportunity-ids
  "This is intended for linking up buyerspheres that are linked
   to a crm already"
  [db organization-id opportunity-ids]
  (-> (h/select :id :crm_opportunity_id)
      (h/from :buyersphere)
      (h/where [:= :organization_id organization-id]
               [:in :crm_opportunity_id opportunity-ids])
      (db/->execute db)))

(defn get-by-shortcode
  "shortcodes should be unique across orgs but we'll still use the
   org id for some extra security"
  [db organization-id shortcode]
  (let [query (-> (base-buyersphere-query organization-id)
                  (h/where [:= :buyersphere.shortcode shortcode]))]
    (->> query
        (db/->>execute db)
         first)))

(comment
  (get-by-id db/local-db 1 1)
  (get-by-organization db/local-db 1)
  (get-by-organization db/local-db 1 {:user-id 1})
  (get-by-organization db/local-db 1 {:stage "evaluation"})
  (get-full-buyersphere db/local-db 1 38)
  (get-by-user db/local-db 1 1)
  (get-by-opportunity-ids db/local-db 1 ["006Hs00001H8xaUIAR" "abc123"])
  (get-by-shortcode db/local-db 1 "abc123")
  (get-by-shortcode db/local-db 2 "abc123")
  (get-by-shortcode db/local-db 1 "abc124")
  ;
  )

(defn- update-buyersphere-field [db organization-id buyersphere-id set-map]
  (let [update-query (-> (h/update :buyersphere)
                         (h/set set-map)
                         (h/where [:= :buyersphere.organization_id organization-id]
                                  [:= :buyersphere.id buyersphere-id])
                        ;;  always include name for buyer tracking
                         (merge (apply h/returning (concat (keys set-map) [:buyer]))))
        updated (->> update-query
                     (db/->>execute db)
                     first)]
    (cond-> updated
      (:qualification-date set-map) (update :qualification_date u/to-date-string)
      (:evaluation-date set-map) (update :evaluation_date u/to-date-string)
      (:decision-date set-map) (update :decision_date u/to-date-string))))

(defn update-buyersphere [db organization-id buyersphere-id
                          {:keys [features-answer qualified-on evaluated-on
                                  decided-on adopted-on qualification-date
                                  evaluation-date decision-date success-criteria-answer
                                  objectives-answer constraints-answer] :as body}]
  (let [fields (cond-> (select-keys body [:pricing-can-pay
                                          :pricing-tier-id
                                          :current-stage
                                          :status
                                          :intro-message
                                          :buyer
                                          :buyer-logo
                                          :show-pricing
                                          :deal-amount
                                          :crm-opportunity-id
                                          :subname
                                          :is-public
                                          :room-type])
                 features-answer (assoc :features-answer [:lift features-answer])
                 success-criteria-answer (assoc :success-criteria-answer [:lift success-criteria-answer])
                 objectives-answer (assoc :objectives-answer [:lift objectives-answer])
                 constraints-answer (assoc :constraints-answer [:lift constraints-answer])
                 qualified-on (assoc :qualified-on (inst/read-instant-date qualified-on))
                 evaluated-on (assoc :evaluated-on (inst/read-instant-date evaluated-on))
                 decided-on (assoc :decided-on (inst/read-instant-date decided-on))
                 adopted-on (assoc :adopted-on (inst/read-instant-date adopted-on))
                 qualification-date (assoc :qualification-date (u/read-date-string qualification-date))
                 evaluation-date (assoc :evaluation-date (u/read-date-string evaluation-date))
                 decision-date (assoc :decision-date (u/read-date-string decision-date)))]
    (update-buyersphere-field db organization-id buyersphere-id fields)))

(comment
  (update-buyersphere db/local-db 1 1 {:features-answer {:interests {1 "yes"}}})
  (update-buyersphere db/local-db 1 1 {:status "on-hold"})
  (update-buyersphere db/local-db 1 1 {:pricing-can-pay "yes" :pricing-tier-id 3 :a :b})
  (update-buyersphere db/local-db 1 1 {:current-stage "evaluation"})
  (update-buyersphere db/local-db 1 1 {:intro-message "howdy!" :is-public true})
  (update-buyersphere db/local-db 1 10 {:buyer "lol" :buyer-logo "lololol" :current-stage "adoption"})
  (update-buyersphere db/local-db 1 10 {:buyer "lol" :show-pricing false})
  (update-buyersphere db/local-db 1 10 {:qualified-on "2023-10-26T00:00:54Z"})
  (update-buyersphere db/local-db 1 10 {:qualification-date "2023-01-02"})
  (update-buyersphere db/local-db 1 11 {:crm-opportunity-id "abc123" :deal-amount 12345})
  (update-buyersphere db/local-db 1 38 {:success-criteria-answer {:text "hello"}
                                        :objectives-answer {:text "goodnight"}
                                        :constraints-answer {:text "goodbye"}})
  ;
  )

(defn- add-default-resources [db organization-id buyersphere-id]
  (when-let [resources (d-res/get-resources-by-organization-id db organization-id)]
    (let [build-values (juxt :organization_id (constantly buyersphere-id) :title :link)]
      (-> (h/insert-into :buyersphere_resource)
          (h/columns :organization_id :buyersphere_id :title :link)
          (h/values (map build-values resources))
          (db/->execute db)))))

;; 0-9 a-z A-Z
(def ^:private nano-alphabet "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
(def ^:private nano-id-gen (nano/custom nano-alphabet 6))

(defn- is-valid-shortcode? [db shortcode]
  (let [query (-> (h/select :id)
                  (h/from :buyersphere)
                  (h/where [:= :shortcode shortcode]))]
    (->> query
         (db/->>execute db)
         seq
         not)))

(defn find-valid-shortcode [db]
  (loop [shortcode (nano-id-gen)]
    (if (is-valid-shortcode? db shortcode)
      shortcode
      (recur (nano-id-gen)))))

(comment
  (is-valid-shortcode? db/local-db "abc123")
  (is-valid-shortcode? db/local-db "abc124")
  (find-valid-shortcode db/local-db)
  ;
  )

(defn- create-buyersphere-record [db organization-id user-id
                                  {:keys [buyer subname buyer-logo deal-amount crm-opportunity-id room-type]}]
  (let [shortcode (find-valid-shortcode db)
        query (-> (h/insert-into :buyersphere)
                  (h/columns :organization_id
                             :buyer
                             :subname
                             :buyer_logo
                             :show_pricing
                             :deal_amount
                             :crm_opportunity_id
                             :shortcode
                             :room_type
                             :owner_id)
                  (h/values [[organization-id
                              buyer
                              subname
                              buyer-logo
                              true
                              deal-amount
                              crm-opportunity-id
                              shortcode
                              room-type
                              user-id]])
                  (merge (apply h/returning only-buyersphere-cols)))]
    (->> query
         (db/->>execute db)
         first)))

(defn- add-default-milestones-coordinator
  "returns a map of the milestone template ids mapped to the newly created
   milestone ids so that the newly created activities can be properly
   mapped upon creation"
  [db organization-id buyersphere-id]
  (let [milestone-templates (d-act-temp/get-milestone-templates db organization-id)]
    (when (seq milestone-templates)
      (let [to-insert (map (fn [mt]
                             (-> mt
                                 (select-keys [:organization_id :title :ordering])
                                 (assoc :buyersphere_id buyersphere-id)))
                           milestone-templates)
            insert-query (-> (h/insert-into :buyersphere_milestone)
                             (h/values to-insert)
                             (h/returning :id))
            new-ids (->> insert-query
                         (db/->>execute db)
                         (map :id))
            old-ids (map :id milestone-templates)]
        (zipmap old-ids new-ids)))))

(defn- add-default-activities-coordinator
  [db organization-id buyersphere-id user-id mt-id->m-id]
  (let [activity-templates (d-act-temp/get-activity-templates db organization-id)]
    (when (seq activity-templates)
      (let [to-insert (map (fn [{:keys [milestone_template_id] :as at}]
                             (-> at 
                                 (select-keys [:organization_id :title
                                               :activity_type :assigned_team])
                                 (assoc :buyersphere_id buyersphere-id)
                                 (assoc :creator_id user-id)
                                 (assoc :milestone_id (mt-id->m-id milestone_template_id))))
                           activity-templates)
            insert-query (-> (h/insert-into :buyersphere_activity)
                             (h/values to-insert)
                             (h/returning :id))]
        (db/execute db insert-query)))))

(defn create-buyersphere-coordinator [db organization-id user-id
                                      {:keys [page-template-id page-title] :as buyersphere-params}]
  (let [{new-id :id} (create-buyersphere-record db organization-id user-id buyersphere-params)
        mt-id->m-id (add-default-milestones-coordinator db organization-id new-id)]
    (when (seq mt-id->m-id)
      (add-default-activities-coordinator
       db organization-id new-id user-id mt-id->m-id))
    (add-default-resources db organization-id new-id)
    (d-teams/add-user-to-buyersphere db organization-id new-id "seller" user-id)
    (d-buyer-pages/create-buyersphere-page-coordinator db
                                                       organization-id
                                                       new-id
                                                       {:title page-title
                                                        :page-template-id page-template-id
                                                        :page-type "general"})
    {:id new-id}))

(comment
  (create-buyersphere-coordinator db/local-db 1 1 {:buyer "adidas" :buyer-logo "https://nike.com"
                                                   :deal-amount 1234 :crm-opportunity-id "abc123"
                                                   :page-template-id 2 :page-title "asdf"
                                                   :room-type "deal-room"})
  ;
  )
