(ns partnorize-api.data.salesforce-access
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn save-salesforce-access-token [db organization-id user-id access-token]
  (-> (h/insert-into :salesforce_access)
      (h/columns :organization_id :user_account_id :access_token)
      (h/values [[organization-id user-id access-token]])
      (h/on-conflict :user_account_id)
      (h/do-update-set :access_token)
      (h/returning :access_token)
      (db/->execute db)
      first))

(defn get-salesforce-access-token [db organization-id user-id]
  (-> (h/select :access_token)
      (h/from :salesforce_access)
      (h/where [:= :organization_id organization-id]
               [:= :user_account_id user-id])
      (db/->execute db)
      first
      :access_token))

(comment
  (save-salesforce-access-token db/local-db 1 1 "abc1234")
  (get-salesforce-access-token db/local-db 1 1)
  ;
  )