(ns partnorize-api.data.buyerspheres
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.buyersphere-notes :as d-buyer-notes]
            [partnorize-api.data.buyersphere-resources :as d-buyer-res]
            [partnorize-api.data.deal-timing :as d-deal-timing]
            [partnorize-api.data.pricing :as d-pricing]
            [partnorize-api.data.resources :as d-res]
            [partnorize-api.data.teams :as d-teams]
            [partnorize-api.data.utilities :as util]
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
   :buyersphere.show_pricing])

(defn- base-buyersphere-query [organization-id]
  (-> (apply h/select base-buyersphere-cols)
      (h/from :buyersphere)
      (h/where [:= :buyersphere.organization_id organization-id])))

(defn get-by-ids [db organization-id ids]
  (-> (base-buyersphere-query organization-id)
      (h/where [:in :buyersphere.id ids])
      (db/->execute db)))

(defn get-by-id [db organization-id id]
  (first (get-by-ids db organization-id [id])))

;; TODO add status to buyersphers
(defn get-by-organization
  ([db organization-id]
   (get-by-organization db organization-id {}))
  ([db organization-id {:keys [user-id stage status]}]
   (let [query (cond-> (base-buyersphere-query organization-id)
                 (util/is-provided? user-id) (h/where [:in :buyersphere.id
                                                       (-> (h/select :buyersphere_id)
                                                           (h/from :buyersphere_user_account)
                                                           (h/where [:= :user_account_id user-id]))])
                 (util/is-provided? stage) (h/where [:= :buyersphere.current_stage stage])
                 (util/is-provided? status) (cond->
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
        (assoc :seller_team seller-team))))

(defn get-by-user
  "This is intended for buyers to check which buyerspheres they 
   are part of so the UI can send them to the right place 
   (otherwise it's hard for them to get there w/o knowing the url)"
  [db organization-id user-id]
  (-> (base-buyersphere-query organization-id)
      (h/join :buyersphere_user_account [:= :buyersphere.id :buyersphere_user_account.buyersphere_id])
      (h/join)
      (h/where [:= :buyersphere_user_account.user_account_id user-id])
      (db/->execute db)))

(comment
  (get-by-id db/local-db 1 1)
  (get-by-organization db/local-db 1)
  (get-by-organization db/local-db 1 {:user-id 1})
  (get-by-organization db/local-db 1 {:stage "evaluation"})
  (get-full-buyersphere db/local-db 1 1)
  (get-by-user db/local-db 1 1)
  ;
  )


(defn- update-buyersphere-field [db organization-id buyersphere-id set-map]
  (-> (h/update :buyersphere)
      (h/set set-map)
      (h/where [:= :buyersphere.organization_id organization-id]
               [:= :buyersphere.id buyersphere-id])
      (merge (apply h/returning (keys set-map)))
      (db/->execute db)
      first))

(def ^:private stage-timestamp-to-update
  {"evaluation" :qualified_on
   "decision" :evaluated_on
   "adoption" :decided_on})

(defn update-buyersphere [db organization-id buyersphere-id
                          {:keys [current-stage features-answer] :as body}]
  (let [fields (cond-> (select-keys body [:pricing-can-pay
                                          :pricing-tier-id
                                          :current-stage
                                          :status
                                          :intro-message
                                          :buyer
                                          :buyer-logo
                                          :show-pricing])
                 current-stage (assoc (stage-timestamp-to-update current-stage)
                                      [[:now]])
                 features-answer (assoc :features-answer [:lift features-answer]))]
    (update-buyersphere-field db organization-id buyersphere-id fields)))

(comment
  (update-buyersphere db/local-db 1 1 {:features-answer {:interests {1 "yes"}}})
  (update-buyersphere db/local-db 1 1 {:status "on-hold"})
  (update-buyersphere db/local-db 1 1 {:pricing-can-pay "yes" :pricing-tier-id 3 :a :b})
  (update-buyersphere db/local-db 1 1 {:current-stage "evaluation"})
  (update-buyersphere db/local-db 1 1 {:intro-message "howdy!"})
  (update-buyersphere db/local-db 1 10 {:buyer "lol" :buyer-logo "lololol" :current-stage "adoption"})
  (update-buyersphere db/local-db 1 10 {:buyer "lol" :show-pricing false})
  ;
  )

(defn- create-buyersphere-record [db organization-id {:keys [buyer buyer-logo]}]
  (let [{:keys [qualified-days evaluation-days decision-days adoption-days]}
        (util/kebab-case (d-deal-timing/get-deal-timing-by-organization-id db organization-id))
        {show-pricing :show-by-default}
        (util/kebab-case (d-pricing/get-global-pricing-by-organization-id db organization-id))]
    (-> (h/insert-into :buyersphere)
        (h/columns :organization_id :buyer
                   :buyer_logo :show_pricing
                   :qualification_date :evaluation_date 
                   :decision_date :adoption_date)
        (h/values [[organization-id
                    buyer
                    buyer-logo
                    show-pricing
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
  (create-buyersphere db/local-db 1 1 {:buyer "nike" :buyer-logo "https://nike.com"})
  ;
  )
