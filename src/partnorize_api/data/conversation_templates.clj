(ns partnorize-api.data.conversation-templates
  (:require [clojure.core :as c]
            [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn base-conversation-template-query [organization-id]
  (-> (h/select :conversation_template_item.id
                :conversation_template_item.message
                :conversation_template_item.due_date_days
                :conversation_template_item.created_at
                :conversation_template_item.assigned_team
                :conversation_template_item.collaboration_type)
      (h/from :conversation_template_item)
      (h/where [:= :conversation_template_item.organization_id organization-id])
      (h/order-by :conversation_template_item.updated_at)))

(defn get-by-organization
  [db organization-id]
  (let [query (base-conversation-template-query organization-id)]
    (db/execute db query)))

(defn create-conversation-template-item
  [db organization-id message due-date-days assigned-team collaboration-type]
  (let [new-item-query (-> (h/insert-into :conversation_template_item)
                           (h/columns :organization_id :message :due_date_days
                                      :assigned_team :collaboration_type)
                           (h/values [[organization-id message due-date-days
                                       assigned-team collaboration-type]])
                           (h/returning :id))
        {new-item-id :id} (->> new-item-query
                               (db/->>execute db)
                               first)
        get-new-query (-> (base-conversation-template-query organization-id)
                          (h/where [:= :conversation_template_item.id new-item-id]))]
    (->> get-new-query
         (db/->>execute db)
         first)))

(defn update-conversation-template-item
  [db organization-id conversation-template-item-id body]
  (let [fields (select-keys body [:message :due-date-days 
                                  :assigned-team :collaboration-type])
        update-query (-> (h/update :conversation_template_item)
                         (h/set fields)
                         (h/where [:= :conversation_template_item.organization_id organization-id]
                                  [:= :conversation_template_item.id conversation-template-item-id])
                         (merge (apply h/returning (keys fields))))
        updated-item (->> update-query
                          (db/->>execute db)
                          first)]
    updated-item))

(comment
  (get-by-organization db/local-db 1)

  (create-conversation-template-item db/local-db 1 "new-thing 5" 7 "buyer" "task")
  (update-conversation-template-item db/local-db 1 2 {:message "goodbye! hello" :due-date-days 15
                                                      :assigned-team "seller"})
  ;
  )
