(ns partnorize-api.data.personas
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn get-personas-by-organization-id [db organization-id]
  (-> (h/select :persona.id :persona.organization_id :persona.ordering
                :persona.title :persona.description)
      (h/from :persona)
      (h/where [:= :persona.organization_id organization-id])
      (h/order-by :persona.ordering)
      (db/->execute db)))

(comment
  (get-personas-by-organization-id db/local-db 1)
  ;
  )
