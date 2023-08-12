(ns partnorize-api.data.teams
  (:require  [honey.sql.helpers :as h]
             [partnorize-api.db :as db]))

(defn- base-team-query [organization-id buyersphere-id]
  (-> (h/select :user_account.email :user_account.buyersphere_role
                :user_account.display_role :user_account.first_name
                :user_account.last_name :buyersphere_user_account.team 
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


(comment
  (get-by-buyersphere db/local-db 1 1)
  ;
  )
