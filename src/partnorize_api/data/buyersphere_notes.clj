(ns partnorize-api.data.buyersphere-notes
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

;; TODO a limit?
(defn- base-note-query [organization-id buyersphere-id]
  (-> (h/select :buyersphere_note.id :buyersphere_note.buyersphere_id
                :buyersphere_note.title :buyersphere_note.body
                :buyersphere_note.created_at
                :user_account.first_name :user_account.last_name
                :user_account.display_role)
      (h/from :buyersphere_note)
      (h/join :user_account [:= :buyersphere_note.author :user_account.id])
      (h/where [:= :buyersphere_note.organization_id organization-id]
               [:= :buyersphere_note.buyersphere_id buyersphere-id])
      (h/order-by :buyersphere_note.updated_at)))

(defn- reformat-author [{:keys [first_name last_name display_role] :as note}]
  (-> note
      (dissoc :first_name :last_name :display_role)
      (assoc :author {:first_name first_name
                      :last_name last_name
                      :display_role display_role})))

(defn get-by-buyersphere [db organization-id buyersphere-id]
  (->> (base-note-query organization-id buyersphere-id)
       (db/->>execute db)
       (map reformat-author)))

(defn create-buyersphere-note [db organization-id buyersphere-id {:keys [author-id title body]}]
  (let [new-id (-> (h/insert-into :buyersphere_note)
                   (h/columns :organization_id :buyersphere_id :author :title :body)
                   (h/values [[organization-id buyersphere-id author-id title body]])
                   (h/returning :id)
                   (db/->execute db)
                   first
                   :id)
        get-new-query (-> (base-note-query organization-id buyersphere-id)
                          (h/where [:= :buyersphere_note.id new-id]))]
    (->> get-new-query
         (db/->>execute db)
         (map reformat-author)
         first)))

(defn update-buyersphere-note [db organization-id buyersphere-id note-id {:keys [title body]}]
  (let [exec-update (-> (h/update :buyersphere_note)
                        (h/set {:title title :body body})
                        (h/where [:= :buyersphere_note.organization_id organization-id]
                                 [:= :buyersphere_note.buyersphere_id buyersphere-id]
                                 [:= :buyersphere_note.id note-id])
                        (db/->execute db))
        get-updated-query (-> (base-note-query organization-id buyersphere-id)
                              (h/where [:= :buyersphere_note.id note-id]))]
    (->> get-updated-query
         (db/->>execute db)
         (map reformat-author)
         first)))

(defn delete-buyersphere-note [db organization-id buyersphere-id note-id]
  (-> (h/delete-from :buyersphere_note)
      (h/where [:= :organization_id organization-id]
               [:= :buyersphere_id buyersphere-id]
               [:= :id note-id])
      (db/->execute db)))

(comment
  (get-by-buyersphere db/local-db 1 1)
  (create-buyersphere-note db/local-db 1 1 {:author-id 1 :title "hello" :body "<p>world!</p>"})
  (update-buyersphere-note db/local-db 1 1 7 {:title "goodbye!!!" :body true})
  (delete-buyersphere-note db/local-db 1 1 3)
  ;
  )
