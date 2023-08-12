(ns partnorize-api.data.personas
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(def ^:private persona-columns
  [:persona.id :persona.organization_id :persona.ordering
   :persona.title :persona.description])

(defn get-personas-by-organization-id [db organization-id]
  (-> (apply h/select persona-columns)
      (h/from :persona)
      (h/where [:= :persona.organization_id organization-id])
      (h/order-by :persona.ordering)
      (db/->execute db)))

(defn create-persona [db organization-id {:keys [title description]}]
  (-> (h/insert-into :persona)
      (h/columns :organization_id :ordering :title :description)
      (h/values [[organization-id
                  (u/get-next-ordering-query :persona organization-id)
                  title
                  description]])
      (#(apply h/returning % persona-columns))
      (db/->execute db)
      first))

(defn update-persona [db organization-id id {:keys [title description]}]
  (-> (h/update :persona)
      (h/set {:title title :description description})
      (h/where [:= :organization_id organization-id]
               [:= :id id])
      (#(apply h/returning % persona-columns))
      (db/->execute db)
      first))

(defn delete-persona [db organization-id id]
  (-> (h/delete-from :persona)
      (h/where [:= :organization_id organization-id]
               [:= :id id])
      (db/->execute db)))

(comment
  (get-personas-by-organization-id db/local-db 1)
  (create-persona db/local-db 1 {:title "ryan" :description "echternacht"})
  (update-persona db/local-db 1 24 {:title "ryan" :description "echternacht"})
  (delete-persona db/local-db 1 19)
  ;
  )
