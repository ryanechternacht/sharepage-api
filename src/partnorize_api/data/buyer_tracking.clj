(ns partnorize-api.data.buyer-tracking
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.buyerspheres :as buyersphere]
            [partnorize-api.data.users :as users]
            [partnorize-api.db :as db]))

;; 
;; This file often reference user-5-tuples. These contain the information 
;; necessary to track user information
;; 
;; [{:organization_id :buyersphere_id :user_account_id :linked_name :entered_name}] 
;; 
;; These are often accepted by or returned by various functions in this
;; file. These are not meant to leave this namespace
;; 

(defn- should-track-user-coordinator?
  "if the user is a buyer or anonymous, returns user 5-tuples
   for the buyersphere. If the user is a seller, returns nil"
  [db organization-id buyersphere-id user-id {:keys [linked-name entered-name]}]
  (let [{:keys [id buyersphere_role]}
        (users/get-by-id db organization-id user-id)]
    (if (= buyersphere_role "admin")
      nil
      {:organization_id organization-id
       :buyersphere_id buyersphere-id
       :user_account_id id
       :linked_name linked-name
       :entered_name entered-name})))

(defn- track-buyer-activity
  "Tracks activity for the user"
  ([db user-5-tuple activity]
   (track-buyer-activity db user-5-tuple activity nil))
  ([db user-5-tuple activity activity-data]
   (let [insert-col (-> user-5-tuple
                        (assoc :activity activity)
                        (assoc :activity-data [:lift activity-data]))]
     (-> (h/insert-into :buyer_tracking)
         (h/values [insert-col])
         (db/->execute db)))))

(defn track-activity-if-buyer-coordinator
  "If the user is a buyer or anonymous, track the activity for the 
   supplied org/buyersphere"
  ([db organization-id buyersphere-id user-id anonymous-user activity]
   (track-activity-if-buyer-coordinator db
                                        organization-id
                                        buyersphere-id
                                        user-id
                                        anonymous-user
                                        activity
                                        nil))
  ([db organization-id buyersphere-id user-id anonymous-user activity activity-data]
   (when-let [buyer (should-track-user-coordinator? db organization-id buyersphere-id user-id anonymous-user)]
     (track-buyer-activity db
                           buyer
                           activity
                           activity-data))))

(comment
  (should-track-user-coordinator? db/local-db
                                  1
                                  1
                                  4
                                  {:linked-name "hello"
                                   :entered-name "world"})
  
  (track-buyer-activity db/local-db 
                        {:organization_id 1, :buyersphere_id 1, :user_account_id nil, :linked_name "hello", :entered_name "world"}
                        "site-activity"
                        {:hello "world"})
  
  (track-activity-if-buyer-coordinator db/local-db
                                       1
                                       1
                                       1
                                       {:linked-name "hello"
                                        :entered-name "world"}
                                       "site-activity"
                                       {:hello "world"})
  ;
  )

(def ^:private base-tracking-columns
  (concat users/user-columns
          [:user_account.id :user_account_id]
          buyersphere/base-buyersphere-cols
          [:buyersphere.id :buyersphere_id]
          [:buyer_tracking.activity
           :buyer_tracking.activity_data
           :buyer_tracking.created_at
           :buyer_tracking.buyersphere_id
           :buyer_tracking.linked_name
           :buyer_tracking.entered_name]))

(defn base-tracking-query [organization-id]
  (-> (apply h/select base-tracking-columns)
      (h/from :buyer_tracking)
      (h/left-join :user_account [:=
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

(defn reformat-tracking [{:keys [user_account_id email buyersphere_id display_role
                                 first_name last_name image buyer
                                 buyer_logo activity activity_data 
                                 created_at linked_name entered_name]}]
  (cond-> {:activity activity
           :activity-data activity_data
           :created_at created_at
           :anonymous-user {:linked_name linked_name
                            :entered_name entered_name}
           :buyersphere {:id buyersphere_id
                         :buyer buyer
                         :buyer_logo buyer_logo}}
    user_account_id (assoc :user {:id user_account_id
                                  :email email
                                  :display_role display_role
                                  :first_name first_name
                                  :last_name last_name
                                  :image image})))

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
                      :user_account_id 4
                      :first_name "Holster",
                      :last_name "Tully",
                      :is_admin false,
                      :display_role "Lord of Riverrun",
                      :buyersphere_role "buyer",
                      :image "person.gif"
                      :organization_id 1,
                      :activity "site-activity"
                      :buyersphere_id 1
                      :buyer "nike",
                      :buyer_logo "nike.com/image",
                      :created_at #inst "2023-12-20T06:01:44.926274000-00:00"
                      :entered_name "entered-name"
                      :linked_name "linked-name"})
    (reformat-tracking {:email "holster@tully.com",
                      :organization_id 1,
                      :activity "site-activity"
                      :buyersphere_id 1
                      :buyer "nike",
                      :buyer_logo "nike.com/image",
                      :created_at #inst "2023-12-20T06:01:44.926274000-00:00"
                      :entered_name "entered-name"
                      :linked_name "linked-name"})
  (get-tracking-for-buyersphere db/local-db 1 1)
  (get-tracking-for-organization db/local-db 1)
  ;
  )
