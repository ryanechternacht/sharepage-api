(ns partnorize-api.data.salesforce-access
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn save-salesforce-access-details [db organization-id user-id access-token instance-url]
  (-> (h/insert-into :salesforce_access)
      (h/columns :organization_id :user_account_id :access_token :instance_url)
      (h/values [[organization-id user-id access-token instance-url]])
      (h/on-conflict :user_account_id)
      (h/do-update-set :access_token :instance_url)
      (h/returning :access_token :instance_url)
      (db/->execute db)
      first))

(defn get-salesforce-access-details [db organization-id user-id]
  (-> (h/select :access_token :instance_url)
      (h/from :salesforce_access)
      (h/where [:= :organization_id organization-id]
               [:= :user_account_id user-id])
      (db/->execute db)
      first))

(comment
  (save-salesforce-access-details db/local-db 1 1 "abc1234" "http://www.google.com")
  (get-salesforce-access-details db/local-db 1 1)
  ;
  )