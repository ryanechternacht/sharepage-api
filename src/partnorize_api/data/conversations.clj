(ns partnorize-api.data.conversations
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

;; TODO a limit?
(defn- base-conversation-query [organization-id buyersphere-id]
  (-> (h/select :buyersphere_conversation.id
                :buyersphere_conversation.buyersphere_id
                :buyersphere_conversation.message
                :buyersphere_conversation.resolved
                :user_account.first_name :user_account.last_name
                :user_account.display_role)
      (h/from :buyersphere_conversation)
      (h/join :user_account [:= :buyersphere_conversation.author :user_account.id])
      (h/where [:= :buyersphere_conversation.organization_id organization-id]
               [:= :buyersphere_conversation.buyersphere_id buyersphere-id])
      (h/order-by :buyersphere_conversation.updated_at)))

(defn- reformat-author [{:keys [first_name last_name display_role] :as conversation}]
  (-> conversation
      (dissoc :name :display_role)
      (assoc :author {:first_name first_name
                      :last_name last_name
                      :display_role display_role})))

(defn get-by-buyersphere [db organization-id buyersphere-id]
  (->> (base-conversation-query organization-id buyersphere-id)
       (db/->>execute db)
       (map reformat-author)))

(defn create-conversation [db organization-id buyersphere-id author-id message]
  (let [new-id (-> (h/insert-into :buyersphere_conversation)
                   (h/columns :organization_id :buyersphere_id :author :message)
                   (h/values [[organization-id buyersphere-id author-id message]])
                   (h/returning :id)
                   (db/->execute db)
                   first
                   :id)
        get-new-query (-> (base-conversation-query organization-id buyersphere-id)
                          (h/where [:= :buyersphere_conversation.id new-id]))]
    (->> get-new-query
         (db/->>execute db)
         (map reformat-author)
         first)))

(comment
  (get-by-buyersphere db/local-db 1 1)
  (create-conversation db/local-db 1 1 1 "hello, world!")
  ;
  )
