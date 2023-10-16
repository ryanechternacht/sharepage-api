(ns partnorize-api.data.conversations
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.users :as users]
            [partnorize-api.db :as db]
            [clojure.instant :as inst]
            [clojure.core :as c]))

;; TODO a limit?
(defn- base-conversation-query [organization-id buyersphere-id]
  (-> (h/select :buyersphere_conversation.id
                :buyersphere_conversation.buyersphere_id
                :buyersphere_conversation.message
                :buyersphere_conversation.resolved
                :buyersphere_conversation.due_date
                :buyersphere_conversation.created_at
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
      (h/join [:user_account :user_account_assigned_to]
              [:= :buyersphere_conversation.assigned_to :user_account_assigned_to.id])
      (h/where [:= :buyersphere_conversation.organization_id organization-id]
               [:= :buyersphere_conversation.buyersphere_id buyersphere-id])
      (h/order-by :buyersphere_conversation.updated_at)))

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
      (dissoc :assigned_to_first_name :assigned_to_last_name :assigned_to_display_role)
      (assoc :assigned_to {:id assigned_to_id
                           :first_name assigned_to_first_name
                           :last_name assigned_to_last_name
                           :display_role assigned_to_display_role})))

(defn get-by-buyersphere [db organization-id buyersphere-id]
  (->> (base-conversation-query organization-id buyersphere-id)
       (db/->>execute db)
       (map reformat-author)
       (map reformat-assigned-to)))

(defn create-conversation [db organization-id buyersphere-id author-id message due-date assigned-to-id]
  (let [due-date-inst (inst/read-instant-date due-date)
        new-id (-> (h/insert-into :buyersphere_conversation)
                   (h/columns :organization_id :buyersphere_id :author :message :due_date :assigned_to)
                   (h/values [[organization-id buyersphere-id author-id message due-date-inst assigned-to-id]])
                   (h/returning :id)
                   (db/->execute db)
                   first
                   :id)
        get-new-query (-> (base-conversation-query organization-id buyersphere-id)
                          (h/where [:= :buyersphere_conversation.id new-id]))]
    (->> get-new-query
         (db/->>execute db)
         (map reformat-author)
         (map reformat-assigned-to)
         first)))

(defn replace-assigned-to-with-user [db organization-id conversation]
  (let [{:keys [first_name last_name display_role id]} (users/get-by-id db organization-id (:assigned_to conversation))]
    (-> conversation
        (dissoc :assigned_to)
        (assoc :assigned_to {:id id
                             :first_name first_name
                             :last_name last_name
                             :display_role display_role}))))

(defn update-conversation [db organization-id buyersphere-id conversation-id body]
  (let [fields (cond-> (select-keys body [:resolved :message :due-date :assigned-to])
                 :due-date (update :due-date inst/read-instant-date))
        result (-> (h/update :buyersphere_conversation)
                   (h/set fields)
                   (h/where [:= :buyersphere_conversation.organization_id organization-id]
                            [:= :buyersphere_conversation.buyersphere_id buyersphere-id]
                            [:= :buyersphere_conversation.id conversation-id])
                   (merge (apply h/returning (keys fields)))
                   (db/->execute db)
                   first)]
    (cond->> result
      :assigned-to (replace-assigned-to-with-user db organization-id))))

(comment
  (get-by-buyersphere db/local-db 1 1)
  (create-conversation db/local-db 1 1 1 "hello, world!" "2012-02-03" 5)
  (update-conversation db/local-db 1 1 29 {:message "goodbye! hello" :resolved false :due-date "2023-10-05T22:00:00.000Z" :assigned-to 2})
  ;
  )
