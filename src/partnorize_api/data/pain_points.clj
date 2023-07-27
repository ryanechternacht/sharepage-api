(ns partnorize-api.data.pain-points
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(def ^:private pain-point-columns
  [:pain_point.id :pain_point.organization_id :pain_point.ordering
   :pain_point.title :pain_point.description])

(defn get-pain-points-by-organization-id [db organization-id]
  (-> (apply h/select pain-point-columns)
      (h/from :pain_point)
      (h/where [:= :pain_point.organization_id organization-id])
      (h/order-by :pain-point.ordering)
      (db/->execute db)))

(defn create-pain-point [db organization-id {:keys [title description]}]
  (-> (h/insert-into :pain_point)
      (h/columns :organization_id :ordering :title :description)
      (h/values [[organization-id
                  (u/get-next-ordering-query :pain-point organization-id)
                  title
                  description]])
      (#(apply h/returning % pain-point-columns))
      (db/->execute db)
      first))

(comment
  (get-pain-points-by-organization-id db/local-db 1)
  (create-pain-point db/local-db 1 {:title "ryan" :description "echternacht"})
  ;
  )
