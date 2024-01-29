(ns partnorize-api.data.buyersphere-activity-templates
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.users :as users]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(def ^:private base-milestone-template-cols
  [:id :organization_id :title :ordering])

(defn- base-milestone-template-query [organization-id]
  (-> (apply h/select base-milestone-template-cols)
      (h/from :buyersphere_milestone_template)
      (h/where [:= :organization_id organization-id])
      (h/order-by :ordering)))

(defn get-milestone-templates [db organization-id]
  (->> (base-milestone-template-query organization-id)
       (db/->>execute db)))

(defn create-milestone-template [db organization-id {:keys [title]}]
  (let [query (-> (h/insert-into :buyersphere_milestone_template)
                  (h/columns :organization_id :title :ordering)
                  (h/values [[organization-id title
                              (u/get-next-ordering-query
                               :buyersphere_milestone_template
                               organization-id)]])
                  (merge (apply h/returning base-milestone-template-cols)))]
    (->> query
         (db/execute db)
         first)))

(defn update-milestone-template [db organization-id id milestone-template]
  (let [fields (-> (select-keys milestone-template [:title]))
        update-query (-> (h/update :buyersphere_milestone_template)
                         (h/set fields)
                         (h/where [:= :organization_id organization-id]
                                  [:= :id id])
                         (merge (apply h/returning (keys fields))))]
    (->> update-query
         (db/->>execute db)
         first)))

(defn delete-milestone-template [db organization-id id]
  (let [query (-> (h/delete-from :buyersphere_milestone_template)
                  (h/where [:= :organization_id organization-id]
                           [:= :id id])
                  (h/returning :id))]
    (->> query
         (db/->>execute db)
         first)))

(comment
  (get-milestone-templates db/local-db 1)

  (create-milestone-template db/local-db 1 {:title "hello, world 2"})

  (update-milestone-template db/local-db 1 1 {:title "hello, world 4"})

  (delete-milestone-template db/local-db 1 2)
  ;
  )

(def ^:private base-activity-template-cols
  [:id :organization_id :milestone_template_id :activity_type
   :title :assigned_team])

(defn- base-activity-template-query [organization-id]
  (-> (apply h/select base-activity-template-cols)
      (h/from :buyersphere_activity_template)
      (h/where [:= :organization_id organization-id])
      (h/order-by :title)))

(defn get-activity-templates [db organization-id]
  (let [query (base-activity-template-query organization-id)]
    (db/execute db query)))

(defn create-activity-template [db organization-id milestone-template-id
                                {:keys [activity-type title assigned-team]}]
  (let [query (-> (h/insert-into :buyersphere_activity_template)
                  (h/columns :organization_id :milestone_template_id
                             :activity_type :title :assigned_team)
                  (h/values [[organization-id milestone-template-id
                              activity-type title  assigned-team]])
                  (merge (apply h/returning base-activity-template-cols)))]
    (->> query
         (db/->>execute db)
         first)))

(defn update-activity-template [db organization-id id activity-template]
  (let [fields (select-keys activity-template [:milestone-template-id
                                               :activity-type
                                               :title
                                               :assigned-team])
        query (-> (h/update :buyersphere_activity_template)
                  (h/set fields)
                  (h/where [:= :organization_id organization-id]
                           [:= :id id])
                  (merge (apply h/returning (keys fields))))]
    (->> query
         (db/->>execute db)
         first)))

(defn delete-activity-template [db organization-id id]
  (let [query (-> (h/delete-from :buyersphere_activity_template)
                  (h/where [:= :organization_id organization-id]
                           [:= :id id])
                  (h/returning :id))]
    (->> query
         (db/->>execute db)
         first)))

(comment
  (get-activity-templates db/local-db 1)

  (create-activity-template db/local-db 1 1 {:activity-type "comment"
                                             :title "ryan's item"
                                             :assigned-team "seller"})

  (update-activity-template db/local-db 1 1 {:activity-type "question"
                                             :assigned-team "buyer"})
  
  (delete-activity-template db/local-db 1 2)
  ;
  )
