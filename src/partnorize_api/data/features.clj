(ns partnorize-api.data.features
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.db :as db]))

(def ^:private feature-columns
  [:feature.id :feature.organization_id :feature.ordering
   :feature.title :feature.description])

(defn get-features-by-organization-id [db organization-id]
  (-> (apply h/select feature-columns)
      (h/from :feature)
      (h/where [:= :feature.organization_id organization-id])
      (h/order-by :feature.ordering)
      (db/->execute db)))

(defn create-feature [db organization-id {:keys [title description]}]
  (-> (h/insert-into :feature)
      (h/columns :organization_id :ordering :title :description)
      (h/values [[organization-id
                  (u/get-next-ordering-query :feature organization-id)
                  title
                  (u/sanitize-html description)]])
      (#(apply h/returning % feature-columns))
      (db/->execute db)
      first))

(comment
  (get-features-by-organization-id db/local-db 1)
  (create-feature db/local-db 1 {:title "ryan" :description "echternacht"})
  ;
  )
