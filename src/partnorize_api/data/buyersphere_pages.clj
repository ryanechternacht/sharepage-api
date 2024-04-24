(ns partnorize-api.data.buyersphere-pages
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.buyersphere-page-templates :as bpt]
            [partnorize-api.data.utilities :as u]))

(def ^:private base-buyersphere-page-cols
  [:id :organization_id :buyersphere_id :title :body :is_public :ordering
   :can_buyer_edit :page_type :status])

(defn- base-buyersphere-page-query [organization-id buyersphere-id]
  (-> (apply h/select base-buyersphere-page-cols)
      (h/from :buyersphere_page)
      (h/where [:= :organization_id organization-id]
               [:= :buyersphere-id buyersphere-id]
               [:!= :status "deleted"])
      (h/order-by :ordering)))

(defn get-buyersphere-pages [db organization-id buyersphere-id]
  (let [query (base-buyersphere-page-query organization-id buyersphere-id)]
    (->> query
         (db/->>execute db))))

(defn get-buyersphere-page [db organization-id buyersphere-id page-id]
  (let [query (-> (base-buyersphere-page-query organization-id buyersphere-id)
                  (h/where [:= :id page-id]))]
    (->> query
         (db/->>execute db)
         first)))

;; this sorta sucks, but generating the column list differently 
;; also feels like it sucks
(def ^:private default-body {:sections []})

(defn create-buyersphere-page-coordinator
  [db organization-id buyersphere-id {:keys [title page-type page-template-id]}]
  (let [body (if (and page-template-id (> page-template-id 0))
               (:body (bpt/get-buyersphere-page-template db organization-id page-template-id))
               default-body)
        query (-> (h/insert-into :buyersphere_page)
                  (h/columns :organization_id :buyersphere_id :title :page_type :body :ordering)
                  (h/values [[organization-id buyersphere-id title page-type [:lift body]
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
  (let [fields (cond-> (select-keys page [:is-public :title :can-buyer-edit :page-type :status])
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
  (get-buyersphere-page db/local-db 1 62 57)

  (create-buyersphere-page-coordinator db/local-db 1 1 {:title "hello, world 4" :page-type "discussion"})
  (create-buyersphere-page-coordinator db/local-db 1 1
                                       {:title "hello, world 4" :page-template-id 2
                                        :page-type "notes"})

  (update-buyersphere-page db/local-db 1 1 117 {:body {:hello "world"}
                                                :title "asdf"
                                                :can-buyer-edit true
                                                :page-type "notes"
                                                :status "deleted"})

  (delete-buyersphere-page db/local-db 1 1 3)
  ;
  )
