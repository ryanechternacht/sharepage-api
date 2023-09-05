(ns partnorize-api.data.resources
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(def ^:private base-resource-columns
  [:deal_resource.id :deal_resource.organization_id
   :deal_resource.title :deal_resource.link])

(defn get-resources-by-organization-id [db organization-id]
  (-> (apply h/select base-resource-columns)
      (h/from :deal_resource)
      (h/where [:= :deal_resource.organization_id organization-id])
      (h/order-by :deal_resource.id)
      (db/->execute db)))

(defn create-resource [db organization-id {:keys [title link]}]
  (-> (h/insert-into :deal_resource)
      (h/columns :organization_id :title :link)
      (h/values [[organization-id title link]])
      (merge (apply h/returning base-resource-columns))
      (db/->execute db)
      first))

(defn update-resource [db organization-id resource-id {:keys [title link]}]
  (-> (h/update :deal_resource)
      (h/set {:title title :link link})
      (h/where [:= :organization_id organization-id]
               [:= :id resource-id])
      (merge (apply h/returning base-resource-columns))
      (db/->execute db)
      first))

(defn delete-resource [db organization-id resource-id]
  (-> (h/delete-from :deal_resource)
      (h/where [:= :organization_id organization-id]
               [:= :id resource-id])
      (db/->execute db)))

(comment 
  (get-resources-by-organization-id db/local-db 1)
  (create-resource db/local-db 1 {:title "asdf" :link "asdf2"})
  (update-resource db/local-db 1 3 {:title "asdf2" :link "asdf3"})
  (delete-resource db/local-db 1 3)
  ;
  )