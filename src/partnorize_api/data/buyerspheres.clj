(ns partnorize-api.data.buyerspheres
  (:require [honey.sql.helpers :as h]
            [clojure.instant :as inst]
            [partnorize-api.data.buyersphere-notes :as d-buyer-notes]
            [partnorize-api.data.buyersphere-resources :as d-buyer-res]
            [partnorize-api.data.deal-timing :as d-deal-timing]
            [partnorize-api.data.pricing :as d-pricing]
            [partnorize-api.data.resources :as d-res]
            [partnorize-api.data.teams :as d-teams]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.db :as db]))

(def ^:private base-buyersphere-cols
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
   :buyersphere.crm_opportunity_id])

(defn- base-buyersphere-query [organization-id]
  (-> (apply h/select base-buyersphere-cols)
      (h/from :buyersphere)
      (h/where [:= :buyersphere.organization_id organization-id])))

(defn get-by-ids [db organization-id ids]
  (let [buyerspheres (-> (base-buyersphere-query organization-id)
                         (h/where [:in :buyersphere.id ids])
                         (db/->execute db))]
    (->> buyerspheres
         (map #(update % :qualification_date u/to-date-string))
         (map #(update % :evaluation_date u/to-date-string))
         (map #(update % :decision_date u/to-date-string)))))

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
     (db/->execute query db))))

;; TODO is this how I want this to work?
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
        (update :decision_date u/to-date-string))))

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


(comment
  (get-by-id db/local-db 1 1)
  (get-by-organization db/local-db 1)
  (get-by-organization db/local-db 1 {:user-id 1})
  (get-by-organization db/local-db 1 {:stage "evaluation"})
  (get-full-buyersphere db/local-db 1 1)
  (get-by-user db/local-db 1 1)
  (get-by-opportunity-ids db/local-db 1 ["006Hs00001H8xaUIAR" "abc123"])
  ;
  )


(defn- update-buyersphere-field [db organization-id buyersphere-id set-map]
  (let [updated (cond-> (-> (h/update :buyersphere)
                            (h/set set-map)
                            (h/where [:= :buyersphere.organization_id organization-id]
                                     [:= :buyersphere.id buyersphere-id])
                            (merge (apply h/returning (keys set-map)))
                            (db/->execute db)
                            first))]
    (cond-> updated
      (:qualification-date set-map) (update :qualification_date u/to-date-string)
      (:evaluation-date set-map) (update :evaluation_date u/to-date-string)
      (:decision-date set-map) (update :decision_date u/to-date-string))))

(defn update-buyersphere [db organization-id buyersphere-id
                          {:keys [features-answer qualified-on evaluated-on
                                  decided-on adopted-on qualification-date 
                                  evaluation-date decision-date
                                  deal-amount crm-opportunity-id] :as body}]
  (let [fields (cond-> (select-keys body [:pricing-can-pay
                                          :pricing-tier-id
                                          :current-stage
                                          :status
                                          :intro-message
                                          :buyer
                                          :buyer-logo
                                          :show-pricing
                                          :deal-amount
                                          :crm-opportunity-id])
                 features-answer (assoc :features-answer [:lift features-answer])
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
  (update-buyersphere db/local-db 1 1 {:intro-message "howdy!"})
  (update-buyersphere db/local-db 1 10 {:buyer "lol" :buyer-logo "lololol" :current-stage "adoption"})
  (update-buyersphere db/local-db 1 10 {:buyer "lol" :show-pricing false})
  (update-buyersphere db/local-db 1 10 {:qualified-on "2023-10-26T00:00:54Z"})
  (update-buyersphere db/local-db 1 10 {:qualification-date "2023-01-02"})
  (update-buyersphere db/local-db 1 10 {:crm-opportunity-id "abc123" :deal-amount 12345})
  ;
  )

(defn- create-buyersphere-record [db organization-id
                                  {:keys [buyer buyer-logo deal-amount crm-opportunity-id]}]
  (let [{:keys [qualified-days evaluation-days decision-days adoption-days]}
        (u/kebab-case (d-deal-timing/get-deal-timing-by-organization-id db organization-id))
        {show-pricing :show-by-default}
        (u/kebab-case (d-pricing/get-global-pricing-by-organization-id db organization-id))
        evaluation-days (+ qualified-days evaluation-days)
        decision-days (+ evaluation-days decision-days)
        adoption-days (+ decision-days adoption-days)]
    (-> (h/insert-into :buyersphere)
        (h/columns :organization_id :buyer
                   :buyer_logo :show_pricing
                   :deal_amount :crm_opportunity_id
                   :qualification_date :evaluation_date
                   :decision_date :adoption_date)
        (h/values [[organization-id
                    buyer
                    buyer-logo
                    show-pricing
                    deal-amount
                    crm-opportunity-id
                    [:raw (str "NOW() + INTERVAL '" qualified-days " DAYS'")]
                    [:raw (str "NOW() + INTERVAL '" evaluation-days " DAYS'")]
                    [:raw (str "NOW() + INTERVAL '" decision-days " DAYS'")]
                    [:raw (str "NOW() + INTERVAL '" adoption-days " DAYS'")]]])
        (merge (apply h/returning base-buyersphere-cols))
        (db/->execute db)
        first)))

(defn- add-default-resources [db organization-id buyersphere-id]
  (let [resources (d-res/get-resources-by-organization-id db organization-id)
        build-values (juxt :organization_id (constantly buyersphere-id) :title :link)]
    (-> (h/insert-into :buyersphere_resource)
        (h/columns :organization_id :buyersphere_id :title :link)
        (h/values (map build-values resources))
        (db/->execute db))))

(defn create-buyersphere [db organization-id user-id buyersphere]
  (let [{new-id :id} (create-buyersphere-record db organization-id buyersphere)]
    (add-default-resources db organization-id new-id)
    (d-teams/add-user-to-buyersphere db organization-id new-id "seller" user-id)
    new-id))

(comment
  (create-buyersphere db/local-db 1 1 {:buyer "nike" :buyer-logo "https://nike.com"
                                       :deal-amount 1234 :crm-opportunity-id "abc123"})
  ;
  )
