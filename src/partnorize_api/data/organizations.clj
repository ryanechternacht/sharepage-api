(ns partnorize-api.data.organizations
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(def ^:private base-organizations-query
  (-> (h/select :organization.id :organization.name
                :organization.subdomain :organization.domain)
      (h/from :organization)))

(defn get-by-subdomain [db subdomain]
  (-> base-organizations-query
      (h/where [:= :organization.subdomain subdomain])
      (db/->execute db)
      first))

(comment 
  (get-by-subdomain db/local-db "dunder-mifflin")
  ;
  )