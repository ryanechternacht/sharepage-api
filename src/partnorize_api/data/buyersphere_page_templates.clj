(ns partnorize-api.data.buyersphere-page-templates
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(def ^:private base-buyersphere-page-template-cols
  [:id :organization_id :title :body :is_public :ordering])

(defn- base-buyersphere-page-template-query [organization-id]
  (-> (apply h/select base-buyersphere-page-template-cols)
      (h/from :buyersphere_page_template)
      (h/where [:= :organization_id organization-id])
      (h/order-by :ordering)))

(defn get-buyersphere-page-templates [db organization-id]
  (let [query (base-buyersphere-page-template-query organization-id)]
    (->> query
         (db/->>execute db))))

(defn get-buyersphere-page-template [db organization-id id]
  (let [query (-> (base-buyersphere-page-template-query organization-id)
                  (h/where [:= :id id]))]
    (->> query
         (db/->>execute db)
         first)))

(defn create-buyersphere-page-template [db organization-id {:keys [title]}]
  (let [query (-> (h/insert-into :buyersphere_page_template)
                  (h/columns :organization_id :title :ordering)
                  (h/values [[organization-id title
                              (u/get-next-ordering-query
                               :buyersphere_page_template
                               organization-id)]])
                  (merge (apply h/returning base-buyersphere-page-template-cols)))]
    (->> query
         (db/->>execute db)
         first)))

(defn update-buyersphere-page-template [db organization-id id
                                        {:keys [body] :as page-template}]
  (let [fields (cond-> (select-keys page-template [:title])
                 body (assoc :body [:lift body]))
        update-query (-> (h/update :buyersphere_page_template)
                         (h/set fields)
                         (h/where [:= :organization_id organization-id]
                                  [:= :id id])
                         (merge (apply h/returning (keys fields))))
        updated-item (->> update-query
                          (db/->>execute db)
                          first)]
    updated-item))

(defn delete-buyersphere-page-template [db organization-id id]
  (let [query (-> (h/delete-from :buyersphere_page_template)
                  (h/where [:= :organization_id organization-id]
                           [:= :id id]))]
    (->> query
         (db/->>execute db))))

(comment
  (get-buyersphere-page-templates db/local-db 1)

  (get-buyersphere-page-template db/local-db 1 2)

  (create-buyersphere-page-template db/local-db 1 {:title "hello, world 4"})

  (update-buyersphere-page-template db/local-db 1 1 {:body {:hello "world"}
                                                     :title "asdf"})
  
  (delete-buyersphere-page-template db/local-db 1 1)
  ;
  )
