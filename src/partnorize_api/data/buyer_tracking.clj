(ns partnorize-api.data.buyer-tracking
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.buyerspheres :as buyersphere]
            [partnorize-api.data.users :as users]
            [partnorize-api.db :as db]))

(defn get-if-buyer
  "if the user is a buyer for this org, returns 
   [{:user_account.id :buyersphere.id :organization.id}] for each 
   buyersphere the user is in. If the user doesn't exist or isn't a buyer, 
   returns []"
  [db email-address stytch-organization-id]
  (-> (h/select [:user_account.id :user_account_id]
                :user_account.organization_id
                :buyersphere_user_account.buyersphere_id)
      (h/from :user_account)
      (h/join :organization [:= :user_account.organization_id :organization.id])
      (h/join :buyersphere_user_account 
              [:= :user_account.id :buyersphere_user_account.user_account_id])
      (h/where [:= :organization.stytch_organization_id stytch-organization-id]
               [:= :user_account.email email-address]
               [:= :user_account.buyersphere_role "buyer"])
      (db/->execute db)))

(defn track-buyer-activity-batched
  "user triplets should be of the form 
   [{:user_account.id :buyersphere.id :organization.id}]"
  [db activity user-triplets]
  (let [insert-cols (map #(assoc % :activity activity) user-triplets)]
    (-> (h/insert-into :buyer_tracking)
        (h/values insert-cols)
        (db/->execute db))))

(defn track-buyer-activity [db activity organization-id buyersphere-id user-id]
  (track-buyer-activity-batched db
                                activity
                                [{:organization_id organization-id
                                  :buyersphere_id buyersphere-id
                                  :user_account_id user-id}]))

(defn if-user-is-buyer-track-login [db {:keys [email_address organization_id]}]
  (when-let [buyers (seq (get-if-buyer db email_address organization_id))]
    (track-buyer-activity-batched db
                                  "site-activity"
                                  buyers)))

(comment
  (get-if-buyer db/local-db "holster@tully.com" "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781")
  (track-buyer-activity db/local-db "site-activity" 1 1 5)
  (if-user-is-buyer-track-login db/local-db
                                {:email_address "holster@tully.com"
                                 :organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"})
  
  (get-if-buyer db/local-db "asdf" "asdf")
  (if-user-is-buyer-track-login db/local-db
                                {:email_address "asdf"
                                 :organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"})
  ;
  )

(def ^:private base-tracking-columns
  (concat users/user-columns
          buyersphere/base-buyersphere-cols
          [:buyer_tracking.activity
           :buyer_tracking.activity_data
           :buyer_tracking.created_at
           :buyer_tracking.buyersphere_id]))

(defn base-tracking-query [organization-id]
  (-> (apply h/select base-tracking-columns)
      (h/from :buyer_tracking)
      (h/join :user_account [:=
                             :buyer_tracking.user_account_id
                             :user_account.id])
      (h/join :buyersphere [:=
                            :buyer_tracking.buyersphere_id
                            :buyersphere.id])
      (h/where [:= :buyer_tracking.organization_id organization-id])
      (h/order-by [:created_at :desc])
      (h/limit 100)))

(defn get-tracking-for-buyersphere-query [organization-id buyersphere-id]
  (-> (base-tracking-query organization-id)
      (h/where [:= :buyer_tracking.buyersphere_id buyersphere-id])))

(defn reformat-tracking [{:keys [id email buyersphere_id display_role 
                                 first_name last_name image buyer 
                                 buyer_logo activity created_at]}]
  {:activity activity
   :created_at created_at
   :user {:id id
          :email email
          :display_role display_role
          :first_name first_name
          :last_name last_name
          :image image}
   :buyersphere {:id buyersphere_id
                 :buyer buyer
                 :buyer_logo buyer_logo}})

(defn get-tracking-for-buyersphere [db organization-id buyersphere-id]
  (let [query (get-tracking-for-buyersphere-query organization-id buyersphere-id)]
    (->> query
         (db/->>execute db)
         (map reformat-tracking))))

(defn get-tracking-for-organization [db organization-id]
  (let [query (base-tracking-query organization-id)]
    (->> query
         (db/->>execute db)
         (map reformat-tracking))))

(comment
  (get-tracking-for-buyersphere-query 1 1)
  (reformat-tracking {:email "holster@tully.com",
                      :first_name "Holster",
                      :organization_id 1,
                      :is_admin false,
                      :activity "site-activity",
                      :id 4,
                      :last_name "Tully",
                      :display_role "Lord of Riverrun",
                      :image nil,
                      :buyersphere_role "buyer",
                      :created_at #inst "2023-12-20T06:01:44.926274000-00:00"})
  (get-tracking-for-buyersphere db/local-db 1 1)
  (get-tracking-for-organization db/local-db 1)
  ;
  )
