(ns partnorize-api.data.buyersphere-templates
  (:require [cljstache.core :as stache]
            [honey.sql.helpers :as h]
            [partnorize-api.data.buyerspheres :as buyerspheres]
            [partnorize-api.data.buyersphere-pages :as pages]
            [partnorize-api.data.buyersphere-links :as links]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.db :as db]
            [partnorize-api.external-api.open-ai :as open-ai]
            [partnorize-api.middleware.config :as config]))

(defmulti render-section (fn [config data section] (:type section)))

(defmethod render-section "text" [config data section]
  (update section :text stache/render data))

(defmethod render-section "header" [config data section]
  (update section :text stache/render data))

(defmethod render-section "asset" [config data section]
  (update section :link stache/render data))

;; TODO use injected config!!!!
(defmethod render-section "ai-prompt" [config data section]
  (let [prompt (stache/render (:prompt section) data)]
    (-> section
        (assoc :type "text")
        (assoc :text (open-ai/generate-message (:open-ai config) prompt)))))

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

(defn create-swaypage-from-template-coordinator [config db organization-id template-id user-id {:keys [template-data] :as body}]
  (let [swaypage (create-buyersphere-record db organization-id user-id body)
        pages (map u/kebab-case (pages/get-buyersphere-active-pages db organization-id template-id))
        links (map u/kebab-case (links/get-buyersphere-links db organization-id template-id))]
    (doseq [page pages]
      (let [rendered-page (update-in page
                                     [:body :sections]
                                     (fn [x] (map #(render-section config template-data %) x)))]
        (create-buyersphere-page db organization-id (:id swaypage) rendered-page)))
    (links/create-buyersphere-links db organization-id (:id swaypage) links)
    swaypage))

(comment
  (let [data {:first-name "ryan 2"
              :last-name "echternacht"
              :company "nike"
              :data-1 "some data"
              :data-2 "other data"
              :data-3 "more data"}]
    (create-swaypage-from-template-coordinator config/config
                                               db/local-db
                                               1
                                               3
                                               1
                                               {:template-data data
                                                :buyer "adidas"
                                                :buyer-logo "https://nike.com"}))
  ;
  )
