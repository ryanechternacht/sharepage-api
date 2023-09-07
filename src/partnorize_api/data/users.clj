(ns partnorize-api.data.users
  (:require  [honey.sql.helpers :as h]
             [partnorize-api.middleware.config :as config]
             [partnorize-api.db :as db]
             [partnorize-api.external-api.stytch :as stytch]))

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

(defn create-user [config db organization {:keys [first-name last-name display-role email]}]
  (when-let [stytch-member-id (stytch/create-user (:stytch config)
                                                  (:stytch-organization-id organization)
                                                  email
                                                  (str first-name " " last-name))]
    (-> (h/insert-into :user_account)
        (h/columns :organization_id :buyersphere_role :first_name
                   :last_name :display_role :email :stytch_member_id)
        (h/values [[(:id organization) "admin" first-name
                    last-name display-role email stytch-member-id]])
        (#(apply h/returning % user-columns))
        (db/->execute db)
        first)))

(comment
  (get-by-email db/local-db 1 "ryan@echternacht.org")
  (get-by-organization db/local-db 1)
  (create-user config/config
               db/local-db
               {:id 1 :stytch-organization-id "organization-test-bd2b29e6-8c0a-48e6-a1c4-d9689883785e"}
               {:first-name "grace"
                :last-name "ooi"
                :email "grac2e@echternacht.org"
                :display-role "my love"})
  ;
  )
