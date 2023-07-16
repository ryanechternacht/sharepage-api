(ns partnorize-api.data.conversations
  (:require [honey.sql.helpers :as h]
           [partnorize-api.db :as db]))

;; TODO a limit?
(defn- base-conversation-query [organization-id buyersphere-id]
  (-> (h/select :buyersphere_conversation.buyersphere_id 
                :buyersphere_conversation.message
                :buyersphere_conversation.resolved
                :user_account.name :user_account.display_role)
      (h/from :buyersphere_conversation)
      (h/join :user_account [:= :buyersphere_conversation.author :user_account.id])
      (h/where [:= :buyersphere_conversation.organization_id organization-id]
               [:= :buyersphere_conversation.buyersphere_id buyersphere-id])
      (h/order-by :buyersphere_conversation.updated_at)))

(defn get-by-buyersphere [db organization-id buyersphere-id]
  (-> (base-conversation-query organization-id buyersphere-id)
      (db/->execute db)))

(comment
  (get-by-buyersphere db/local-db 1 1)
  ;
  )