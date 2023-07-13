(ns partnorize-api.data.features
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn get-features-by-organization-id [db organization-id]
  (-> (h/select :feature.id :feature.organization_id :feature.ordering
                :feature.title :feature.description)
      (h/from :feature)
      (h/where [:= :feature.organization_id organization-id])
      (h/order-by :feature.ordering)
      (db/->execute db)))

(comment
  (get-features-by-organization-id db/local-db 1)
  ;
  )
