(ns partnorize-api.data.organizations
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(def ^:private base-organization-cols
  [:organization.id :organization.name :organization.logo
   :organization.subdomain :organization.domain
   :organization.stytch_organization_id])

(def ^:private base-organizations-query
  (-> (apply h/select base-organization-cols)
      (h/from :organization)))

(defn get-by-subdomain [db subdomain]
  (-> base-organizations-query
      (h/where [:= :organization.subdomain subdomain])
      (db/->execute db)
      first))

(defn update-organization [db id body]
  (let [fields (select-keys body [:name :logo])]
    (-> (h/update :organization)
        (h/set fields)
        (h/where [:= :id id])
        (#(apply h/returning % base-organization-cols))
        (db/->execute db)
        first)))

(comment 
  (get-by-subdomain db/local-db "stark")
  (update-organization db/local-db 1 {:name "Stark" :a :b})
  ;
  )
