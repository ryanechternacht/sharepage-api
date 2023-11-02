(ns partnorize-api.data.salesforce-access
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn save-salesforce-access-details! [db organization-id user-id access-token instance-url refresh-token]
  (-> (h/insert-into :salesforce_access)
      (h/columns :organization_id :user_account_id :access_token :instance_url :refresh_token)
      (h/values [[organization-id user-id access-token instance-url refresh-token]])
      (h/on-conflict :user_account_id)
      (h/do-update-set :access_token :instance_url :refresh_token)
      (h/returning :access_token :instance_url :refresh_token)
      (db/->execute db)
      first))

(defn save-salesforce-access-token! [db organization-id user-id access-token]
  (-> (h/insert-into :salesforce_access)
      (h/columns :organization_id :user_account_id :access_token)
      (h/values [[organization-id user-id access-token]])
      (h/on-conflict :user_account_id)
      (h/do-update-set :access_token)
      (h/returning :access_token)
      (db/->execute db)
      first
      :access_token))

(defn get-salesforce-access-details [db organization-id user-id]
  (-> (h/select :access_token :instance_url :refresh_token)
      (h/from :salesforce_access)
      (h/where [:= :organization_id organization-id]
               [:= :user_account_id user-id])
      (db/->execute db)
      first))

(comment
  (save-salesforce-access-details! db/local-db 1 1 "abc1234" "http://www.google.com" "refresh!")
  (save-salesforce-access-token! db/local-db 1 1 "123")
  (get-salesforce-access-details db/local-db 1 1)
  ;
  )