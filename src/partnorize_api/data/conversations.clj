(ns partnorize-api.data.conversations
  (:require [clojure.core :as c]
            [honey.sql.helpers :as h]
            [partnorize-api.data.users :as users]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

;; TODO a limit?
(defn- base-conversation-query [organization-id]
  (-> (h/select :buyersphere_conversation.id
                :buyersphere_conversation.buyersphere_id
                :buyersphere_conversation.message
                :buyersphere_conversation.resolved
                :buyersphere_conversation.due_date
                :buyersphere_conversation.created_at
                :buyersphere_conversation.assigned_team
                :buyersphere_conversation.collaboration_type
                [:buyersphere.buyer :buyersphere_buyer]
                [:buyersphere.buyer_logo :buyersphere_buyer_logo]
                [:user_account_author.id :author_id]
                [:user_account_author.first_name :author_first_name]
                [:user_account_author.last_name :author_last_name]
                [:user_account_author.display_role :author_display_role]
                [:user_account_assigned_to.id :assigned_to_id]
                [:user_account_assigned_to.first_name :assigned_to_first_name]
                [:user_account_assigned_to.last_name :assigned_to_last_name]
                [:user_account_assigned_to.display_role :assigned_to_display_role])
      (h/from :buyersphere_conversation)
      (h/join [:user_account :user_account_author]
              [:= :buyersphere_conversation.author :user_account_author.id])
      (h/join :buyersphere
              [:= :buyersphere_conversation.buyersphere_id :buyersphere.id])
      (h/left-join [:user_account :user_account_assigned_to]
                   [:= :buyersphere_conversation.assigned_to :user_account_assigned_to.id])
      (h/where [:= :buyersphere_conversation.organization_id organization-id])
      (h/order-by :buyersphere_conversation.updated_at)))

(defn- conversations-for-buyersphere-query [organization-id buyersphere-id]
  (h/where (base-conversation-query organization-id)
           [:= :buyersphere_conversation.buyersphere_id buyersphere-id]))

(defn- reformat-author [{:keys [author_id
                                author_first_name
                                author_last_name
                                author_display_role] :as conversation}]
  (-> conversation
      (dissoc :author_first_name :author_last_name :author_display_role)
      (assoc :author {:id author_id
                      :first_name author_first_name
                      :last_name author_last_name
                      :display_role author_display_role})))

(defn- reformat-assigned-to [{:keys [assigned_to_id
                                     assigned_to_first_name
                                     assigned_to_last_name
                                     assigned_to_display_role] :as conversation}]
  (-> conversation
      (dissoc :assigned_to_first_name :assigned_to_last_name :assigned_to_display_role :assigned_to_id)
      (cond->
       assigned_to_id (assoc :assigned_to {:id assigned_to_id
                                           :first_name assigned_to_first_name
                                           :last_name assigned_to_last_name
                                           :display_role assigned_to_display_role}))))

(defn- reformat-conversation [conversation]
  (-> conversation
      reformat-author
      reformat-assigned-to
      (update :due_date u/to-date-string)))

(defn get-by-buyersphere [db organization-id buyersphere-id]
  (->> (conversations-for-buyersphere-query organization-id buyersphere-id)
       (db/->>execute db)
       (map reformat-conversation)))

(defn get-by-organization 
  ([db organization-id] (get-by-organization db organization-id {}))
  ([db organization-id {:keys [user-id]}]
  (let [query (cond-> (base-conversation-query organization-id)
                (u/is-provided? user-id) (h/where [:= :buyersphere_conversation.assigned_to user-id]))]
    (->> query
         (db/->>execute db)
         (map reformat-conversation)))))

(defn create-conversation [db organization-id buyersphere-id author-id message
                           due-date assigned-to-id assigned-team collaboration-type]
  (let [due-date-inst (u/read-date-string due-date)
        new-id (-> (h/insert-into :buyersphere_conversation)
                   (h/columns :organization_id :buyersphere_id :author :message :due_date
                              :assigned_to :assigned_team :collaboration_type)
                   (h/values [[organization-id buyersphere-id author-id message
                               due-date-inst assigned-to-id assigned-team collaboration-type]])
                   (h/returning :id)
                   (db/->execute db)
                   first
                   :id)
        get-new-query (-> (conversations-for-buyersphere-query organization-id buyersphere-id)
                          (h/where [:= :buyersphere_conversation.id new-id]))]
    (->> get-new-query
         (db/->>execute db)
         (map reformat-conversation)
         first)))

(defn replace-assigned-to-id-with-user [conversation db organization-id]
  (let [{:keys [first_name last_name display_role id]} (users/get-by-id db organization-id (:assigned_to conversation))]
    (-> conversation
        (dissoc :assigned_to_id)
        (cond->
         id (assoc :assigned_to {:id id
                                 :first_name first_name
                                 :last_name last_name
                                 :display_role display_role})))))

(defn update-conversation [db organization-id buyersphere-id conversation-id body]
  (let [fields (cond-> (select-keys body [:resolved :message :due-date :assigned-to :assigned-team :collaboration-type])
                 (:due-date body) (update :due-date u/read-date-string))
        result (-> (h/update :buyersphere_conversation)
                   (h/set fields)
                   (h/where [:= :buyersphere_conversation.organization_id organization-id]
                            [:= :buyersphere_conversation.buyersphere_id buyersphere-id]
                            [:= :buyersphere_conversation.id conversation-id])
                   (merge (apply h/returning (keys fields)))
                   (db/->execute db)
                   first)]
    (cond-> result
      (:assigned-to body) (replace-assigned-to-id-with-user db organization-id)
      (:due-date body) (update :due_date u/to-date-string))))

(comment
  (get-by-buyersphere db/local-db 1 1)
  (get-by-organization db/local-db 1)
  (get-by-organization db/local-db 1 {:user-id 4})
  (create-conversation db/local-db 1 1 1 "hello, world!" "2012-02-03" 5 "buyer" "comment")
  (create-conversation db/local-db 1 1 1 "hello, world!" "2012-02-03" nil "seller" "comment")
  (update-conversation db/local-db 1 1 29 {:message "goodbye! hello" :resolved false :due-date "2023-10-05" :assigned-to 2})
  (update-conversation db/local-db 1 1 29 {:message "goodbye! hello" :resolved false :assigned-to 2 :assigned-team "buyer"})
  (update-conversation db/local-db 1 1 29 {:message "goodbye! hello" :resolved false :assigned-to nil :assigned-team "buyer"})
  (update-conversation db/local-db 1 1 29 {:message "goodbye! hello" :resolved false :collaboration-type "meeting"})
  ;
  )
