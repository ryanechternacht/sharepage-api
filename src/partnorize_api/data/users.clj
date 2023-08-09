(ns partnorize-api.data.users
  (:require  [honey.sql.helpers :as h]
             [partnorize-api.db :as db]))

(def ^:private user-columns
  [:user_account.id :user_account.email :user_account.buyersphere_role
   :user_account.display_role :user_account.organization_id
   :user_account.first_name :user_account.last_name])

(defn- base-user-query [organization-id] 
  (-> (apply h/select user-columns)
      (h/from :user_account)
      (h/where [:= :user_account.organization_id organization-id])
      (h/order-by :first_name :last_name)))

(defn get-by-email [db organization-id email]
  (-> (base-user-query organization-id)
      (h/where [:= :user_account.email email])
      (db/->execute db)
      first))

(defn get-by-organization [db organization-id]
  (-> (base-user-query organization-id)
      (h/where [:= :user_account.buyersphere_role "admin"])
      (db/->execute db)))

(defn create-user [db organization-id {:keys [first_name last_name display_role email]}]
  (-> (h/insert-into :user_account)
      (h/columns :organization_id :buyersphere_role :first_name
                 :last_name :display_role :email)
      (h/values [[organization-id "admin" first_name
                  last_name display_role email]])
      (#(apply h/returning % user-columns))
      (db/->execute db)
      first))

(comment
  (get-by-email db/local-db 1 "ryan@echternacht.org")
  (get-by-organization db/local-db 1)
  (create-user db/local-db 1 {:first_name "grace"
                              :last_name "ooi"
                              :email "grace@ooi"
                              :display_role "my love"})
  ;
  )
