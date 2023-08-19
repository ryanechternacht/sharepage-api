(ns partnorize-api.data.buyerspheres
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.teams :as d-teams]))

(def ^:private base-buyersphere-cols
  [:buyersphere.id :buyersphere.organization_id :buyersphere.buyer
   :buyersphere.buyer_logo :buyersphere.intro_message
   :buyersphere.features_answer :buyersphere.pricing_answer
   :buyersphere.current_stage :buyersphere.qualification_date
   :buyersphere.evaluation_date :buyersphere.decision_date
   :buyersphere.qualified_on :buyersphere.evaluation_on
   :buyersphere.decision_on])

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

(defn get-by-organization
  ([db organization-id]
   (get-by-organization db organization-id {}))
  ([db organization-id {:keys [user-id stage is-overdue]}]
   (println user-id stage is-overdue)
   (let [query (cond-> (base-buyersphere-query organization-id)
                 user-id (h/where [:in :buyersphere.id
                                   (-> (h/select :buyersphere_id)
                                       (h/from :buyersphere_user_account)
                                       (h/where [:= :user_account_id user-id]))])
                 stage (h/where [:= :buyersphere.current_stage stage])
                 is-overdue (h/where [:or
                                      [:and
                                       [:= :current_stage "qualification"]
                                       [:< :qualification_date [[:now]]]]
                                      [:and
                                       [:= :current_stage "evaluation"]
                                       [:< :evaluation_date [[:now]]]]
                                      [:and
                                       [:= :current_stage "decision"]
                                       [:< :decision_date [[:now]]]]])
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

;; TODO should this return full buyersphere?
(defn save-buyersphere-feature-answer [db organization-id buyersphere-id featureAnswers]
  (-> (h/update :buyersphere)
      (h/set {:features_answer [:lift featureAnswers]})
      (h/where [:= :buyersphere.organization_id organization-id]
               [:= :buyersphere.id buyersphere-id])
      (h/returning :features_answer)
      (db/->execute db)))

(comment
  (get-by-id db/local-db 1 1)
  (get-by-organization db/local-db 1)
  (get-by-organization db/local-db 1 {:user-id 1})
  (get-by-organization db/local-db 1 {:stage "evaluation"})
  (get-by-organization db/local-db 1 {:is_overdue true})
  (get-full-buyersphere db/local-db 1 1)
  (save-buyersphere-feature-answer db/local-db 1 1 {:interests {1 "maybe"}})
  ;
  )
