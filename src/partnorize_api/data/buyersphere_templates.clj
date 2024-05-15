(ns partnorize-api.data.buyersphere-templates
  (:require [cljstache.core :as stache]
            [honey.sql.helpers :as h]
            [partnorize-api.data.buyerspheres :as buyerspheres]
            [partnorize-api.data.buyersphere-pages :as pages]
            [partnorize-api.db :as db]
            [partnorize-api.data.utilities :as u]))

(defmulti render-section (fn [data section] (:type section)))

(defmethod render-section "text" [data section]
  (update section :text stache/render data))

(defmethod render-section "header" [data section]
  (update section :text stache/render data))

(defmethod render-section "asset" [data section]
  (update section :link stache/render data))

(defn- create-buyersphere-record [db organization-id user-id
                                  {:keys [buyer subname buyer-logo]}]
  (let [shortcode (buyerspheres/find-valid-shortcode db)
        query (-> (h/insert-into :buyersphere)
                  (h/columns :organization_id
                             :buyer
                             :subname
                             :buyer_logo
                             :shortcode
                             :room_type
                             :owner_id)
                  (h/values [[organization-id
                              buyer
                              subname
                              buyer-logo
                              shortcode
                              "deal-room"
                              user-id]])
                  (merge (apply h/returning buyerspheres/only-buyersphere-cols)))]
    (->> query
         (db/->>execute db)
         first)))

(defn create-buyersphere-page
  [db organization-id buyersphere-id {:keys [title page-type is-public can-buyer-edit body status]}]
  (let [query (-> (h/insert-into :buyersphere_page)
                  (h/columns :organization_id :buyersphere_id :title :page_type :is_public :can_buyer_edit :status :body :ordering)
                  (h/values [[organization-id buyersphere-id title page-type is-public can-buyer-edit status [:lift body]
                              (u/get-next-ordering-query
                               :buyersphere_page
                               organization-id
                               [:= :buyersphere_id buyersphere-id])]])
                  (merge (apply h/returning pages/base-buyersphere-page-cols)))]
    (->> query
         (db/->>execute db)
         first)))

(defn create-swaypage-from-template [db organization-id template-id user-id data]
  (let [bs (create-buyersphere-record db organization-id user-id data)
        pages (map u/kebab-case (pages/get-buyersphere-active-pages db/local-db organization-id template-id))]
    (doseq [page pages]
      (let [rendered-page (update-in page
                                     [:body :sections]
                                     (fn [x] (map #(render-section data %) x)))]
        (create-buyersphere-page db organization-id (:id bs) rendered-page)))
    bs))

(comment
  (let [data {:first-name "ryan 2"
              :last-name "echternacht"
              :company "nike"
              :data-1 "some data"
              :data-2 "other data"
              :data-3 "more data"}]
    (create-swaypage-from-template db/local-db 1 3 1 (assoc data
                                                            :buyer "adidas"
                                                            :buyer-logo "https://nike.com")))
  ;
  )
