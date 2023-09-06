(ns partnorize-api.data.buyersphere-resources
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(def ^:private base-buyersphere-resource-cols
  [:buyersphere_resource.id :buyersphere_resource.organization_id
   :buyersphere_resource.title :buyersphere_resource.link
   :buyersphere_resource.created_at])

(defn- base-buyersphere-resource-query [organization-id]
  (-> (apply h/select base-buyersphere-resource-cols)
      (h/from :buyersphere_resource)
      (h/where [:= :buyersphere_resource.organization_id organization-id])
      (h/order-by :buyersphere_resource.id)))

(defn get-buyersphere-resources-by-buyersphere-id [db organization-id buyersphere-id]
  (-> (base-buyersphere-resource-query organization-id)
      (h/where [:= :buyersphere_resource.buyersphere_id buyersphere-id])
      (h/order-by :buyersphere_resource.title)
      (db/->execute db)))

(defn create-buyersphere-resource [db organization-id buyersphere-id {:keys [title link]}]
  (-> (h/insert-into :buyersphere_resource)
      (h/columns :organization_id :buyersphere_id :title :link)
      (h/values [[organization-id buyersphere-id title link]])
      (merge (apply h/returning base-buyersphere-resource-cols))
      (db/->execute db)
      first))

(defn update-buyersphere-resource [db organization-id buyersphere-id resource-id {:keys [title link]}]
  (-> (h/update :buyersphere_resource)
      (h/set {:title title :link link})
      (h/where [:= :organization_id organization-id]
               [:= :buyersphere_id buyersphere-id]
               [:= :id resource-id])
      (merge (apply h/returning base-buyersphere-resource-cols))
      (db/->execute db)
      first))

(defn delete-buyersphere-resource [db organization-id buyersphere-id resource-id]
  (-> (h/delete-from :buyersphere_resource)
      (h/where [:= :organization_id organization-id]
               [:= :buyersphere_id buyersphere-id]
               [:= :id resource-id])
      (db/->execute db)))

(comment
  (get-buyersphere-resources-by-buyersphere-id db/local-db 1 1)
  (create-buyersphere-resource db/local-db 1 1 {:title "new" :link "link"})
  (update-buyersphere-resource db/local-db 1 1 3 {:title "House Stark items on Etsy" :link "https://www.etsy.com/market/house_stark_sign"})
  (delete-buyersphere-resource db/local-db 1 1 3)
  ;
  )