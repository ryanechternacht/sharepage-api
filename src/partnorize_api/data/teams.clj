(ns partnorize-api.data.teams
  (:require  [honey.sql.helpers :as h]
             [partnorize-api.db :as db]
             [partnorize-api.data.users :as users]
             [partnorize-api.data.utilities :as u]))

(defn- base-team-query [organization-id buyersphere-id]
  (-> (h/select :user_account.id :user_account.email
                :user_account.buyersphere_role :user_account.display_role
                :user_account.first_name :user_account.last_name
                :user_account.image :buyersphere_user_account.team
                :buyersphere_user_account.ordering)
      (h/from :buyersphere_user_account)
      (h/join :user_account [:=
                             :buyersphere_user_account.user_account_id
                             :user_account.id])
      (h/where [:= :buyersphere_user_account.organization_id organization-id]
               [:= :buyersphere_user_account.buyersphere_id buyersphere-id])
      (h/order-by :ordering)))

(defn get-by-buyersphere
  "Returns {:buyer-team [{person-1} ...] :seller-team [{person-1} ...]}. The returned
   people are already sorted by their `ordering` value"
  [db organization-id buyersphere-id]
  (let [people (->> (base-team-query organization-id buyersphere-id)
                    (db/->>execute db)
                    (group-by :team))]
    {:buyer-team (people "buyer")
     :seller-team (people "seller")}))

(defn add-user-to-buyersphere
  "Team should be either 'buyer' or 'seller'."
  [db organization-id buyersphere-id team user-id]
  (-> (h/insert-into :buyersphere_user_account)
      (h/columns :organization_id :buyersphere_id :team :user_account_id :ordering)
      (h/values [[organization-id
                  buyersphere-id
                  team
                  user-id
                  (u/get-next-ordering-query :buyersphere_user_account organization-id
                                             [:= :buyersphere-id buyersphere-id]
                                             [:= :team team])]])
      (db/->execute db)))

(comment
  (get-by-buyersphere db/local-db 1 1)
  (add-user-to-buyersphere db/local-db 1 1 "buyer" 19)
  ;
  )

(defn edit-user-in-buyersphere
  [db organization-id user-id {:keys [first-name last-name display-role]}]
  (let [query (-> (h/update :user_account)
                  (h/set {:first_name first-name
                          :last_name last-name
                          :display_role display-role})
                  (h/where [:= :organization_id organization-id]
                           [:= :id user-id]))]
    (->> (db/execute db query)
         first)))

(comment
  (edit-user-in-buyersphere db/local-db 1 6 {:first-name "minisa"
                                             :last-name "tully"
                                             :display-role "of house whent"})
  ;
  )

(defn remove-user-from-buyersphere-coordinator
  [db organization-id buyersphere-id user-id]
  (let [user (users/get-by-id db organization-id user-id)
        query (-> (h/delete-from :buyersphere_user_account)
                  (h/where [:= :organization_id organization-id]
                           [:= :buyersphere_id buyersphere-id]
                           [:= :user_account_id user-id])
                  (h/returning :user_account_id))]
    (db/execute db query)
    user))

(comment
  (remove-user-from-buyersphere-coordinator db/local-db 1 1 16)
  ;
  )
