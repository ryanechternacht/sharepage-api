(ns partnorize-api.data.buyersphere-activities
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.users :as users]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(def ^:private base-milestone-cols [:id :organization_id :buyersphere_id :title
                                    :ordering :resolved])

(defn- base-milestone-query [organization-id buyersphere-id]
  (-> (apply h/select base-milestone-cols)
      (h/from :buyersphere_milestone)
      (h/where [:= :organization_id organization-id]
               [:= :buyersphere_id buyersphere-id])
      (h/order-by :ordering)))

(defn get-milestones-for-buyersphere [db organization-id buyersphere-id]
  (->> (base-milestone-query organization-id buyersphere-id)
       (db/->>execute db)))

(defn create-milestone [db organization-id buyersphere-id
                        {:keys [title]}]
  (let [query (-> (h/insert-into :buyersphere_milestone)
                  (h/columns :organization_id :buyersphere_id :title
                             :ordering)
                  (h/values [[organization-id buyersphere-id title
                              (u/get-next-ordering-query
                               :buyersphere_milestone
                               organization-id
                               [:= :buyersphere_id buyersphere-id])]])
                  (merge (apply h/returning base-milestone-cols)))]
    (->> query
         (db/execute db)
         first)))

(defn update-milestone [db organization-id buyersphere-id id milestone]
  (let [fields (-> (select-keys milestone [:title :resolved]))
        update-query (-> (h/update :buyersphere_milestone)
                         (h/set fields)
                         (h/where [:= :organization_id organization-id]
                                  [:= :buyersphere_id buyersphere-id]
                                  [:= :id id])
                         ;;  always include :title for tracking (gross)
                         (merge (apply h/returning (concat (keys fields) [:title]))))]
    (->> update-query
         (db/->>execute db)
         first)))

(defn delete-milestone [db organization-id buyersphere-id id]
  (let [query (-> (h/delete-from :buyersphere_milestone)
                  (h/where [:= :organization_id organization-id]
                           [:= :buyersphere_id buyersphere-id]
                           [:= :id id])
                  ;;  always include :title for tracking (gross)
                  (h/returning :id :title))]
    (->> query
         (db/->>execute db)
         first)))

(comment
  (get-milestones-for-buyersphere db/local-db 1 1)

  (create-milestone db/local-db 1 1 {:title "hello, world"})

  (update-milestone db/local-db 1 1 3 {:title "df"})

  (delete-milestone db/local-db 1 1 3)
  ;
  )

(def ^:private base-activities-cols
  [:id :organization_id :buyersphere_id :milestone_id :creator-id :activity_type
   :title :assigned_to :assigned_team :due_date :resolved])

(defn- base-activities-query [organization-id]
  (-> (h/select :buyersphere_activity.id
                :buyersphere_activity.organization_id
                :buyersphere_activity.milestone_id
                :buyersphere_activity.activity_type
                :buyersphere_activity.title
                :buyersphere_activity.assigned_team
                :buyersphere_activity.due_date
                :buyersphere_activity.resolved
                [:buyersphere.id :buyersphere_id]
                [:buyersphere.buyer :buyersphere_buyer]
                [:buyersphere.buyer_logo :buyersphere_buyer_logo]
                [:user_account_creator.id :creator_id]
                [:user_account_creator.first_name :creator_first_name]
                [:user_account_creator.last_name :creator_last_name]
                [:user_account_creator.display_role :creator_display_role]
                [:user_account_creator.buyersphere_role :creator_buyersphere_role]
                [:user_account_assigned_to.id :assigned_to_id]
                [:user_account_assigned_to.first_name :assigned_to_first_name]
                [:user_account_assigned_to.last_name :assigned_to_last_name]
                [:user_account_assigned_to.display_role :assigned_to_display_role]
                [:user_account_assigned_to.buyersphere_role :assigned_to_buyersphere_role])
      (h/from :buyersphere_activity)
      ;; Don't filter on org id so super admin created activities still show
      (h/join [:user_account :user_account_creator]
              [:= :buyersphere_activity.creator_id :user_account_creator.id])
      (h/join :buyersphere
              [:and
               [:= :buyersphere_activity.buyersphere_id :buyersphere.id]
               [:= :buyersphere_activity.organization_id :buyersphere.organization_id]])
      (h/left-join [:user_account :user_account_assigned_to]
                   [:and
                    [:= :buyersphere_activity.assigned_to_id :user_account_assigned_to.id]
                    [:= :buyersphere_activity.organization_id :user_account_assigned_to.organization_id]])
      (h/where [:= :buyersphere_activity.organization_id organization-id])
      (h/order-by [:buyersphere_activity.due_date :asc-nulls-last]
                  :buyersphere_activity.title)))

(defn- reformat-creator-id [{:keys [creator_id
                                    creator_first_name
                                    creator_last_name
                                    creator_display_role
                                    creator_buyersphere_role] :as activity}]
  (-> activity
      (dissoc :creator_id :creator_first_name :creator_last_name
              :creator_display_role :creator_buyersphere_role)
      (cond->
       creator_id (assoc :creator {:id creator_id
                                   :first_name creator_first_name
                                   :last_name creator_last_name
                                   :display_role creator_display_role
                                   :team (users/role-map creator_buyersphere_role)}))))

(defn- reformat-assigned-to [{:keys [assigned_to_id
                                     assigned_to_first_name
                                     assigned_to_last_name
                                     assigned_to_display_role
                                     assigned_to_buyersphere_role] :as activity}]
  (-> activity
      (dissoc :assigned_to_id :assigned_to_first_name :assigned_to_last_name
              :assigned_to_display_role :assigned_to_buyersphere_role)
      (cond->
       assigned_to_id (assoc :assigned_to {:id assigned_to_id
                                           :first_name assigned_to_first_name
                                           :last_name assigned_to_last_name
                                           :display_role assigned_to_display_role
                                           :team (users/role-map assigned_to_buyersphere_role)}))))

(defn- reformat-buyer [{:keys [buyersphere_buyer
                               buyersphere_buyer_logo] :as activity}]
  (-> activity
      (dissoc :buyersphere_buyer :buyersphere_buyer_logo)
      (assoc :buyer {:name buyersphere_buyer
                     :logo buyersphere_buyer_logo})))

(defn reformat-activity [activity]
  (-> activity
      reformat-creator-id
      reformat-assigned-to
      reformat-buyer
      (u/update-if-not-nil :due_date u/to-date-string)))

(defn get-activities-for-buyersphere [db organization-id buyersphere-id]
  (let [query (-> (base-activities-query organization-id)
                  (h/where [:= :buyersphere_activity.buyersphere_id buyersphere-id]))]
    (->> query
         (db/->>execute db)
         (map reformat-activity))))

(defn create-activity [db organization-id buyersphere-id milestone-id creator-id
                       {:keys [activity-type title assigned-to-id
                               assigned-team due-date resolved]}]
  (let [due-date-inst (when due-date (u/read-date-string due-date))
        resolved (if (nil? resolved) false resolved)
        query (-> (h/insert-into :buyersphere_activity)
                  (h/columns :organization_id :buyersphere_id :milestone_id
                             :creator_id :activity_type :title
                             :assigned_to_id :assigned-team
                             :due-date :resolved)
                  (h/values [[organization-id buyersphere-id milestone-id
                              creator-id activity-type title
                              assigned-to-id assigned-team
                              due-date-inst resolved]])
                  (h/returning :id))
        new-id (->> query
                    (db/execute db)
                    first
                    :id)
        get-new-query (-> (base-activities-query organization-id)
                          (h/where [:= :buyersphere_activity.id new-id]))]
    (->> get-new-query
         (db/->>execute db)
         (map reformat-activity)
         first)))

(defn replace-assigned-to-id-with-user [activity db organization-id]
  (let [{:keys [first_name last_name display_role id]}
        (users/get-by-id db organization-id (:assigned_to_id activity))]
    (-> activity
        (dissoc :assigned_to_id)
        (cond->
         id (assoc :assigned_to {:id id
                                 :first_name first_name
                                 :last_name last_name
                                 :display_role display_role})))))

(defn update-activity-coordinator [db organization-id buyersphere-id id activity]
  (let [fields (-> (select-keys activity [:milestone-id
                                          :activity-type
                                          :title
                                          :assigned-to-id
                                          :assigned-team
                                          :due-date
                                          :resolved])
                   (u/update-if-not-nil :due-date u/read-date-string))
        update-query (-> (h/update :buyersphere_activity)
                         (h/set fields)
                         (h/where [:= :organization_id organization-id]
                                  [:= :buyersphere_id buyersphere-id]
                                  [:= :id id])
                          ;;  always include :title for tracking (gross)
                         (merge (apply h/returning (concat (keys fields) [:title]))))
        updated (->> update-query
                     (db/->>execute db)
                     first)]
    (-> updated
        (replace-assigned-to-id-with-user db organization-id)
        (u/update-if-not-nil :due_date u/to-date-string))))

(defn delete-activity [db organization-id buyersphere-id id]
  (let [query (-> (h/delete-from :buyersphere_activity)
                  (h/where [:= :organization_id organization-id]
                           [:= :buyersphere_id buyersphere-id]
                           [:= :id id])
                  ;;  include :title for tracking (gross)
                  (h/returning :id :title))]
    (->> query
         (db/->>execute db)
         first)))

(comment
  (get-activities-for-buyersphere db/local-db 2 5)

  (create-activity db/local-db 1 1 1 1
                   {:activity-type "question"
                    :title "hello world 2"
                    :assigned-to-id 1
                    :assigned-team "seller"})
  
  (update-activity-coordinator db/local-db 1 1 1
                               {:activity-type "action"
                                :assigned-to-id 3
                                :due-date "2024-12-12"})
  
  (delete-activity db/local-db 1 1 1)
  ;
  )

[[:raw (str "CURRENT_DATE + concat(template.due_date_days, 'DAYS')::interval")]]

(defn get-activities-for-organization 
  ([db organization-id] (get-activities-for-organization db organization-id {}))
  ([db organization-id {:keys [user-id]}]
   (let [query (cond-> (-> (base-activities-query organization-id)
                           (h/where [:<= :buyersphere_activity.due_date [[:raw "CURRENT_DATE + interval '30 days'"]]]
                                    [:is :buyersphere_activity.resolved false]))
                 user-id (-> (h/join :buyersphere_user_account
                                     [:and
                                      [:=
                                       :buyersphere_activity.organization_id
                                       :buyersphere_user_account.organization_id]
                                      [:=
                                       :buyersphere_activity.buyersphere_id
                                       :buyersphere_user_account.buyersphere_id]
                                      [:=
                                       :buyersphere_user_account.user_account_id
                                       user-id]])))]
     (->> query
          (db/->>execute db)
          (map reformat-activity)))))

(comment
  (get-activities-for-organization db/local-db 1)
  (get-activities-for-organization db/local-db 1 {:user-id 2})
  )
