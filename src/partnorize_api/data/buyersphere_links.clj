(ns partnorize-api.data.buyersphere-links
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(def ^:private base-buyersphere-link-cols
  [:id :organization_id :buyersphere_id :title :link_url :ordering])

(defn- base-buyersphere-link-query [organization-id buyersphere-id]
  (-> (apply h/select base-buyersphere-link-cols)
      (h/from :buyersphere_link)
      (h/where [:= :organization_id organization-id]
               [:= :buyersphere-id buyersphere-id])
      (h/order-by :ordering)))

(defn get-buyersphere-links [db organization-id buyersphere-id]
  (let [query (base-buyersphere-link-query organization-id buyersphere-id)]
   (->> query
        (db/->>execute db))))

(defn create-buyersphere-links [db organization-id buyersphere-id links]
  (let [next-ordering (u/get-next-ordering-value
                       db
                       :buyersphere_link
                       organization-id
                       [:= :buyersphere_id buyersphere-id])
        insert-links (vec (map-indexed (fn [i {:keys [title link-url]}]
                                         [organization-id
                                          buyersphere-id
                                          title
                                          link-url
                                          (+ next-ordering i)])
                                       links))
        query (-> (h/insert-into :buyersphere_link)
                  (h/columns :organization_id :buyersphere_id :title :link_url :ordering)
                  (h/values insert-links)
                  (merge (apply h/returning base-buyersphere-link-cols)))]
    (->> query
         (db/->>execute db)
         first)))

(defn create-buyersphere-link [db organization-id buyersphere-id {:keys [title link-url]}]
  (let [query (-> (h/insert-into :buyersphere_link)
                  (h/columns :organization_id :buyersphere_id :title :link_url :ordering)
                  (h/values [[organization-id buyersphere-id title link-url
                              (u/get-next-ordering-query
                               :buyersphere_link
                               organization-id
                               [:= :buyersphere_id buyersphere-id])]])
                  (merge (apply h/returning base-buyersphere-link-cols)))]
    (->> query
         (db/->>execute db)
         first)))

(defn update-buyersphere-link [db organization-id buyersphere-id id link]
  (let [fields (cond-> (select-keys link [:title :link-url :ordering]))
        update-query (-> (h/update :buyersphere_link)
                         (h/set fields)
                         (h/where [:= :organization_id organization-id]
                                  [:= :buyersphere_id buyersphere-id]
                                  [:= :id id])
                         (merge (apply h/returning (keys fields))))
        updated-item (->> update-query
                          (db/->>execute db)
                          first)]
    updated-item))

(defn update-buyersphere-links-ordering [db organization-id buyersphere-id links]
  (let [newlyOrdered (map-indexed (fn [i p] [(:id p) i]) links)]
    (doseq [[id ordering] newlyOrdered]
      (let [query (-> (h/update :buyersphere_link)
                      (h/set {:ordering ordering})
                      (h/where [:= :organization_id organization-id]
                               [:= :buyersphere_id buyersphere-id]
                               [:= :id id]))]
        (db/execute db query)))))

(defn delete-buyersphere-link [db organization-id buyersphere-id id]
  (let [query (-> (h/delete-from :buyersphere_link)
                  (h/where [:= :organization_id organization-id]
                           [:= :buyersphere_id buyersphere-id]
                           [:= :id id]))]
    (->> query
         (db/->>execute db))))

(comment
  (get-buyersphere-links db/local-db 1 94)
  (create-buyersphere-link db/local-db 1 94 {:title "hello 6" :link-url "world 6"})
  (create-buyersphere-links db/local-db 1 3 [{:title "hello 6" :link-url "world 6"}
                                             {:title "hello 7" :link-url "world 7"}])

  (update-buyersphere-link db/local-db 1 1 3 {:title "goodnight" :link-url "moon" :ordering 4})
  (update-buyersphere-links-ordering db/local-db 1 1 [{:id 5} {:id 6} {:id 4}])
  (delete-buyersphere-link db/local-db 1 1 3)
  ;
  )