(ns partnorize-api.data.pain-points
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn get-pain-points-by-organization-id [db organization-id]
  (-> (h/select :pain-point.id :pain-point.organization_id :pain-point.ordering
                :pain-point.title :pain-point.description)
      (h/from :pain-point)
      (h/where [:= :pain-point.organization_id organization-id])
      (h/order-by :pain-point.ordering)
      (db/->execute db)))

(comment
  (get-pain-points-by-organization-id db/local-db 1)
  ;
  )
