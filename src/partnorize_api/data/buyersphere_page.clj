(ns partnorize-api.data.buyersphere-pages
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(def ^:private base-buyersphere-page-cols
  [:id :organization_id :buyersphere_id :title :body :is_public :ordering])

(defn- base-buyersphere-page-query [organization-id buyersphere-id]
  (-> (apply h/select base-buyersphere-page-cols)
      (h/from :buyersphere_page)
      (h/where [:= :organization_id organization-id]
               [:= :buyersphere-id buyersphere-id])
      (h/order-by :ordering)))

(defn get-buyersphere-pages [db organization-id buyersphere-id]
  (let [query (base-buyersphere-page-query organization-id buyersphere-id)]
    (->> query
         (db/->>execute db))))

(defn create-buyersphere-page [db organization-id buyersphere-id {:keys [title]}]
  (let [query (-> (h/insert-into :buyersphere_page)
                  (h/columns :organization_id :buyersphere_id :title :ordering)
                  (h/values [[organization-id buyersphere-id title
                              (u/get-next-ordering-query
                               :buyersphere_page
                               organization-id
                               [:= :buyersphere_id buyersphere-id])]])
                  (merge (apply h/returning base-buyersphere-page-cols)))]
    (->> query
         (db/->>execute db)
         first)))

(defn update-buyersphere-page [db organization-id buyersphere-id id
                               {:keys [body] :as page}]
  (let [fields (cond-> (select-keys page [:is_public :title])
                 body (assoc :body [:lift body]))
        update-query (-> (h/update :buyersphere_page)
                         (h/set fields)
                         (h/where [:= :organization_id organization-id]
                                  [:= :buyersphere-id buyersphere-id]
                                  [:= :id id])
                         (merge (apply h/returning (keys fields))))
        updated-item (->> update-query
                          (db/->>execute db)
                          first)]
    updated-item))

(defn delete-buyersphere-page [db organization-id buyersphere-id id]
  (let [query (-> (h/delete-from :buyersphere_page)
                  (h/where [:= :organization_id organization-id]
                           [:= :buyersphere-id buyersphere-id]
                           [:= :id id]))]
    (->> query
         (db/->>execute db))))

(comment
  (get-buyersphere-pages db/local-db 1 1)
  
  (create-buyersphere-page db/local-db 1 1 {:title "hello, world 4"})

  (update-buyersphere-page db/local-db 1 1 3 {:body {:hello "world"}
                                              :title "asdf"})

  (delete-buyersphere-page db/local-db 1 1 3)
  ;
  )
