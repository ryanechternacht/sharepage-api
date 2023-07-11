(ns partnorize-api.data.buyerspheres
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn- base-buyersphere-query [organization-id]
  (-> (h/select :buyersphere.id :buyersphere.organization_id :buyersphere.buyer
                :buyersphere.buyer_logo :buyersphere.intro_message
                :buyersphere.features_answer :buyersphere.pricing_answer
                :buyersphere.current_stage :buyersphere.qualification_date
                :buyersphere.evaluation_date :buyersphere.decision_date
                :buyersphere.qualified_on :buyersphere.evaluation_on
                :buyersphere.decision_on)
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

(defn get-by-organization [db organization-id]
  (-> (base-buyersphere-query organization-id)
      ;; TODO what to order on?
      (h/order-by :buyersphere.buyer)
      (db/->execute db)
      first))

(defn- get-buyersphere-resources-by-buyersphere-id [db organization-id buyersphere-id]
  (-> (base-buyersphere-resource-query organization-id)
      (h/where [:= :buyersphere_resource.buyersphere_id buyersphere-id])
      (h/order-by :buyersphere_resource.title)
      (db/->execute db)))

;; TODO is this how I want this to work?
(defn get-full-buyersphere [db organization-id id]
  (let [buyersphere (get-by-id db organization-id id)
        resources (get-buyersphere-resources-by-buyersphere-id db organization-id id)]
    (assoc buyersphere :resources resources)))

(comment
  (get-by-id db/local-db 1 1)
  (get-by-organization db/local-db 1)
  (get-full-buyersphere db/local-db 1 1)
  ;
  )
