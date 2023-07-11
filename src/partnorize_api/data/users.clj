(ns partnorize-api.data.users
  (:require  [honey.sql.helpers :as h]
             [partnorize-api.db :as db]))

(defn- base-user-query [organization-id] 
  (-> (h/select :user_account.id :user_account.email :user_account.role
                :user_account.organization_id)
      (h/from :user_account)
      (h/where [:= :user_account.organization_id organization-id])))

(defn get-by-email [db organization-id email]
  (-> (base-user-query organization-id)
      (h/where [:= :user_account.email email])
      (db/->execute db)
      first))
