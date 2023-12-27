(ns partnorize-api.data.conversation-templates
  (:require [clojure.core :as c]
            [honey.sql.helpers :as h]
            [partnorize-api.data.users :as users]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(defn- base-conversation-template-query [organization-id]
  (-> (h/select :conversation_template_item.id
                :conversation_template_item.message
                :conversation_template_item.due_date_days
                :conversation_template_item.created_at
                :conversation_template_item.assigned_team
                :conversation_template_item.collaboration_type
                [:user_account_assigned_to.id :assigned_to_id]
                [:user_account_assigned_to.first_name :assigned_to_first_name]
                [:user_account_assigned_to.last_name :assigned_to_last_name]
                [:user_account_assigned_to.display_role :assigned_to_display_role])
      (h/from :conversation_template_item)
      (h/left-join [:user_account :user_account_assigned_to]
                   [:= :conversation_template_item.assigned_to :user_account_assigned_to.id])
      (h/where [:= :conversation_template_item.organization_id organization-id])
      (h/order-by :conversation_template_item.updated_at)))

(defn- reformat-assigned-to [{:keys [assigned_to_id
                                     assigned_to_first_name
                                     assigned_to_last_name
                                     assigned_to_display_role]
                              :as conversation-template-item}]
  (-> conversation-template-item
      (dissoc :assigned_to_first_name :assigned_to_last_name :assigned_to_display_role :assigned_to_id)
      (cond->
       assigned_to_id (assoc :assigned_to {:id assigned_to_id
                                           :first_name assigned_to_first_name
                                           :last_name assigned_to_last_name
                                           :display_role assigned_to_display_role}))))

(defn- reformat-conversation-template-item [conversation-template-item]
  (-> conversation-template-item
      reformat-assigned-to))

(defn get-by-organization
  [db organization-id]
  (let [query (base-conversation-template-query organization-id)]
    (->> query
         (db/->>execute db)
         (map reformat-conversation-template-item))))

(defn create-conversation-template-item
  [db organization-id message due-date-days assigned-to-id
   assigned-team collaboration-type]
  (let [new-item-query (-> (h/insert-into :conversation_template_item)
                           (h/columns :organization_id :message :due_date_days
                                      :assigned_to :assigned_team :collaboration_type)
                           (h/values [[organization-id message due-date-days
                                       assigned-to-id assigned-team collaboration-type]])
                           (h/returning :id))
        {new-item-id :id} (->> new-item-query
                               (db/->>execute db)
                               first)
        get-new-query (-> (base-conversation-template-query organization-id)
                          (h/where [:= :conversation_template_item.id new-item-id]))]
    (->> get-new-query
         (db/->>execute db)
         (map reformat-conversation-template-item)
         first)))

(defn replace-assigned-to-id-with-user [conversation-template-item db organization-id]
  (let [{:keys [first_name last_name display_role id]}
        (users/get-by-id db organization-id (:assigned_to conversation-template-item))]
    (-> conversation-template-item
        (cond->
         id (assoc :assigned_to {:id id
                                 :first_name first_name
                                 :last_name last_name
                                 :display_role display_role})))))

(defn update-conversation-template-item
  [db organization-id conversation-template-item-id body]
  (let [fields (select-keys body [:message :due-date-days :assigned-to
                                  :assigned-team :collaboration-type])
        update-query (-> (h/update :conversation_template_item)
                         (h/set fields)
                         (h/where [:= :conversation_template_item.organization_id organization-id]
                                  [:= :conversation_template_item.id conversation-template-item-id])
                         (merge (apply h/returning (keys fields))))
        updated-item (->> update-query
                          (db/->>execute db)
                          first)]
    (cond-> updated-item
      (:assigned-to body) (replace-assigned-to-id-with-user db organization-id))))

(comment
  (get-by-organization db/local-db 1)

  (create-conversation-template-item db/local-db 1 "new-thing 3" 65 1 "seller" "task")
  (update-conversation-template-item db/local-db 1 1 {:message "goodbye! hello" :due-date-days 15
                                                      :assigned-to 4 :assigned-team "buyer"})
  ;
  )
