(ns partnorize-api.data.buyerspheres
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.teams :as d-teams]
            [partnorize-api.data.utilities :as util]))

(def ^:private base-buyersphere-cols
  [:buyersphere.id :buyersphere.organization_id :buyersphere.buyer
   :buyersphere.buyer_logo :buyersphere.intro_message
   :buyersphere.features_answer :buyersphere.pricing_answer
   :buyersphere.current_stage :buyersphere.qualification_date
   :buyersphere.evaluation_date :buyersphere.decision_date
   :buyersphere.adoption_date :buyersphere.qualified_on 
   :buyersphere.evaluated_on :buyersphere.decided_on
   :buyersphere.adopted_on])

(defn- base-buyersphere-query [organization-id]
  (-> (apply h/select base-buyersphere-cols)
      (h/from :buyersphere)
      (h/where [:= :buyersphere.organization_id organization-id])))

(defn- base-buyersphere-resource-query [organization-id]
  (-> (h/select :buyersphere_resource.id :buyersphere_resource.organization_id
                :buyersphere_resource.title :buyersphere_resource.link
                :buyersphere_resource.created_at)
      (h/from :buyersphere_resource)
      (h/where [:= :buyersphere_resource.organization_id organization-id])))

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
  ([db organization-id {:keys [user-id stage is-overdue]}]
   (let [query (cond-> (base-buyersphere-query organization-id)
                 (util/is-provided? user-id) (h/where [:in :buyersphere.id
                                   (-> (h/select :buyersphere_id)
                                       (h/from :buyersphere_user_account)
                                       (h/where [:= :user_account_id user-id]))])
                 (util/is-provided? stage) (h/where [:= :buyersphere.current_stage stage])
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

(defn- get-buyersphere-resources-by-buyersphere-id [db organization-id buyersphere-id]
  (-> (base-buyersphere-resource-query organization-id)
      (h/where [:= :buyersphere_resource.buyersphere_id buyersphere-id])
      (h/order-by :buyersphere_resource.title)
      (db/->execute db)))

;; TODO is this how I want this to work?
(defn get-full-buyersphere [db organization-id id]
  (let [buyersphere (get-by-id db organization-id id)
        resources (get-buyersphere-resources-by-buyersphere-id db organization-id id)
        {:keys [buyer-team seller-team]} (d-teams/get-by-buyersphere db organization-id id)]
    (-> buyersphere
        (assoc :resources resources)
        (assoc :buyer_team buyer-team)
        (assoc :seller_team seller-team))))

(defn- update-buyersphere-field [db organization-id buyersphere-id set-map]
  (-> (h/update :buyersphere)
      (h/set set-map)
      (h/where [:= :buyersphere.organization_id organization-id]
               [:= :buyersphere.id buyersphere-id])
      (merge (apply h/returning (keys set-map)))
      (db/->execute db)))

(defn update-buyersphere-feature-answer [db organization-id buyersphere-id answer]
  (update-buyersphere-field db organization-id buyersphere-id {:features_answer [:lift answer]}))

(defn update-buyersphere-status [db organization-id buyersphere-id {status :status}]
  (update-buyersphere-field db organization-id buyersphere-id {:status status}))

;; TODO implement a revert stage mode
(defn update-buyersphere-stage [db organization-id buyersphere-id {stage :stage}]
  (let [timestamp_column (condp = stage
                           "evaluation" :qualified_on
                           "decision" :evaluated_on
                           "adoption" :decided_on)])
  (update-buyersphere-field db organization-id buyersphere-id 
                            {:current_stage stage
                             timestamp_column [[:now]]}))

(comment
  (get-by-id db/local-db 1 1)
  (get-by-organization db/local-db 1)
  (get-by-organization db/local-db 1 {:user-id 1})
  (get-by-organization db/local-db 1 {:stage "evaluation"})
  (get-by-organization db/local-db 1 {:is_overdue true})
  (get-full-buyersphere db/local-db 1 1)
  (update-buyersphere-feature-answer db/local-db 1 1 {:interests {1 "yes"}})
  (update-buyersphere-status db/local-db 1 1 {:status "active"})
  (update-buyersphere-stage db/local-db 1 1 {:stage "qualification"})
  ;
  )
