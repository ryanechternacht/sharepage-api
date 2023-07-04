(ns partnorize-api.data.buyerspheres
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.questions :as questions]))

(def ^:private base-buyersphere-query
  (-> (h/select :buyersphere.id :buyersphere.name :buyersphere.organization_id
                :buyersphere.status :buyersphere.logo)
      (h/from :buyersphere)))

(defn get-by-ids [db ids]
  (-> base-buyersphere-query
      (h/where [:in :buyersphere.id ids])
      (db/->execute db)))

(defn get-by-id [db id]
  (first (get-by-ids db [id])))

(defn get-by-organization [db organization-id]
  (-> base-buyersphere-query
      (h/where [:= :buyersphere.organization_id organization-id])
      ;; TODO what to order on?
      (h/order-by :buyersphere.name)
      (db/->execute db)
      first))

;; TODO is this how I want this to work?
(defn get-full-buyersphere [db id]
  (let [buyersphere (get-by-id db id)
        qs (questions/get-by-buyersphere db id)
        qs-grouped-sorted (update-vals (group-by :page qs) #(hash-map :questions (sort-by :ordering %)))]
    (assoc buyersphere :pages qs-grouped-sorted)))

(comment
  (get-by-id db/local-db 1)
  (get-by-organization db/local-db 1)
  (get-full-buyersphere db/local-db 1)
  ;
  )
