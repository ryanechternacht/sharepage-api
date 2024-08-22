(ns partnorize-api.data.buyersphere-templates
  (:require [cljstache.core :as stache]
            [honey.sql.helpers :as h]
            [partnorize-api.data.buyerspheres :as buyerspheres]
            [partnorize-api.data.buyersphere-pages :as pages]
            [partnorize-api.data.buyersphere-links :as links]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.db :as db]
            [partnorize-api.external-api.open-ai :as open-ai]
            [partnorize-api.middleware.config :as config]
            [clojure.string :as str]))

#_{:clj-kondo/ignore [:unused-binding]}
(defmulti render-section (fn [config data section] (:type section)))

#_{:clj-kondo/ignore [:unused-binding]}
(defmethod render-section "text" [config data section]
  (update section :text stache/render data))

#_{:clj-kondo/ignore [:unused-binding]}
(defmethod render-section "header" [config data section]
  (update section :text stache/render data))

#_{:clj-kondo/ignore [:unused-binding]}
(defmethod render-section "asset" [config data section]
  (update section :link stache/render data))

(defmethod render-section "ai-prompt-template" [config data section]
  (let [prompt (stache/render (:prompt section) data)]
    (-> section
        (assoc :type "text")
        (assoc :text (open-ai/generate-message (:open-ai config) prompt)))))

(defn- create-buyersphere-record [db organization-id user-id
                                  {:keys [buyer subname buyer-logo
                                          campaign-uuid campaign-row-number
                                          quick-create-made-by]}]
  (let [shortcode (u/find-valid-buyersphere-shortcode db)
        query (-> (h/insert-into :buyersphere)
                  (h/columns :organization_id
                             :buyer
                             :subname
                             :buyer_logo
                             :shortcode
                             :room_type
                             :owner_id
                             :campaign_uuid
                             :campaign_row_number
                             :quick_create_made_by)
                  (h/values [[organization-id
                              buyer
                              subname
                              buyer-logo
                              shortcode
                              "deal-room"
                              user-id
                              campaign-uuid
                              campaign-row-number
                              quick-create-made-by]])
                  (merge (apply h/returning buyerspheres/only-buyersphere-cols)))]
    (->> query
         (db/->>execute db)
         first)))

(defn create-buyersphere-page
  [db organization-id buyersphere-id {:keys [title :page-type can-buyer-edit body status header-image]}]
  (let [query (-> (h/insert-into :buyersphere_page)
                  (h/columns :organization_id :buyersphere_id :title :page_type :can_buyer_edit :status :body :header_image :ordering)
                  (h/values [[organization-id buyersphere-id title page-type can-buyer-edit status [:lift body] [:lift header-image]
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
      (let [rendered-page (-> page
                              (update-in
                               [:body :sections]
                               (fn [x] (map #(render-section config template-data %) x)))
                              (update :title stache/render template-data))]
        (create-buyersphere-page db organization-id (:id swaypage) rendered-page)))
    (links/create-buyersphere-links db organization-id (:id swaypage) (map #(update % :title stache/render template-data) links))
    swaypage))

(comment
  (let [data {:first-name "ryan 2"
              :last-name "echternacht"
              :account-name "nike"
              :email "ryan@echternacht.org"
              :field-1 "some data"
              :field-2 "other data"
              :field-3 "more data"}]
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

(def context)
;; (def thread-1-header)
;; (def thread-1-subtext)
;; (def thread-1-header-1)
;; (def thread-1-text-1)
;; (def thread-1-header-2)
;; (def thread-1-header-3)
;; (def thread-1-text-3)
;; (def thread-1-header-4)
;; (def thread-1-image-search-term)

(defn- strip-html-response [s]
  (str/replace s #"(```html)|(```)|\n" ""))

(defn- generate-ai-responses [openai-config template-data]
  (let [context-data (-> template-data
                         (select-keys [:lead-name :lead-job-title :account-name :account-website
                                       :seller-name :seller-job-title :seller-company :seller-website]))
        ;; context-prompt (slurp "resources/ai/context-prompt.mustache")
        ;; rendered-context (stache/render context-prompt context-data)
        ;; context (open-ai/generate-message openai-config rendered-context "Generate this response in plain text")

        ;; thread-1-header (-> "resources/ai/thread-1-header-prompt.mustache"
        ;;                   slurp
        ;;                   (stache/render context-data)
        ;;                   (#(open-ai/generate-message openai-config % context))
        ;;                   strip-html-response)

        ;; thread-1-subtext (-> "resources/ai/thread-1-subtext-prompt.mustache"
        ;;                    slurp
        ;;                    (stache/render (assoc context-data :thread-header thread-header))
        ;;                    (#(open-ai/generate-message openai-config % context))
        ;;                    strip-html-response)

        ;; thread-1-header-1 (-> "resources/ai/thread-1-header-1-prompt.mustache"
        ;;                     slurp
        ;;                     (stache/render context-data)
        ;;                     (#(open-ai/generate-message openai-config % context))
        ;;                     strip-html-response)

        ;; thread-1-text-1 (-> "resources/ai/thread-1-text-1-prompt.mustache"
        ;;                   slurp
        ;;                   (stache/render context-data)
        ;;                   (#(open-ai/generate-message openai-config % context))
        ;;                   strip-html-response)

        ;; thread-1-header-2 (-> "resources/ai/thread-1-header-2-prompt.mustache"
        ;;                     slurp
        ;;                     (stache/render context-data)
        ;;                     (#(open-ai/generate-message openai-config % context))
        ;;                     strip-html-response)

        ;; thread-1-header-3 (-> "resources/ai/thread-1-header-3-prompt.mustache"
        ;;                     slurp
        ;;                     (stache/render context-data)
        ;;                     (#(open-ai/generate-message openai-config % context))
        ;;                     strip-html-response)

        ;; thread-1-text-3 (-> "resources/ai/thread-1-text-3-prompt.mustache"
        ;;                   slurp
        ;;                   (stache/render context-data)
        ;;                   (#(open-ai/generate-message openai-config % context))
        ;;                   strip-html-response)

        ;; thread-1-header-4 (-> "resources/ai/thread-1-header-4-prompt.mustache"
        ;;                     slurp
        ;;                     (stache/render context-data)
        ;;                     (#(open-ai/generate-message openai-config % context))
        ;;                     strip-html-response)

        ;; thread-1-image-search-term (-> "resources/ai/thread-1-image-search-term-prompt.mustache"
        ;;                              slurp
        ;;                              (stache/render context-data)
        ;;                              (#(open-ai/generate-message openai-config % context))
        ;;                              strip-html-response)

        ;; thread-2-header (-> "resources/ai/thread-2-header-prompt.mustache"
        ;;                     slurp
        ;;                     (stache/render context-data)
        ;;                     (#(open-ai/generate-message openai-config % context))
        ;;                     strip-html-response)

        ;; thread-2-subtext (-> "resources/ai/thread-2-header-prompt.mustache"
        ;;                     slurp
        ;;                     (stache/render (assoc context-data :thread-2-header thread-2-header))
        ;;                     (#(open-ai/generate-message openai-config % context))
        ;;                     strip-html-response)

        ;; thread-2-header-1 (-> "resources/ai/thread-2-header-1-prompt.mustache"
        ;;                     slurp
        ;;                     (stache/render context-data)
        ;;                     (#(open-ai/generate-message openai-config % context))
        ;;                     strip-html-response)

        ;; thread-2-text-1 (-> "resources/ai/thread-2-text-1-prompt.mustache"
        ;;                       slurp
        ;;                       (stache/render context-data)
        ;;                       (#(open-ai/generate-message openai-config % context))
        ;;                       strip-html-response)

        thread-2-header-2 (-> "resources/ai/thread-2-header-2-prompt.mustache"
                              slurp
                              (stache/render context-data)
                              (#(open-ai/generate-message openai-config % context))
                              strip-html-response)]
    {
    ;;  :thread-1-header thread-1-header
    ;;  :thread-1-subtext thread--1subtext
    ;;  :thread-1-header-1 thread-1-header-1
    ;;  :thread-1-text-1 thread-1-text-1
    ;;  :thread-1-header-2 thread-1-header-2
    ;;  :thread-1-header-3 thread-1-header-3
    ;;  :thread-1-text-3 thread-1-text-3
    ;;  :thread-1-header-4 thread-1-header-4

    ;; TODO
    ;;  :thread-1-image-search-term thread-1-image-search-term
     
    ;;  :thread-2-header thread-2-header 
    ;;  :thread-2-subtext thread-2-subtext
    ;;  :thread-2-header-1 thread-2-header-1
    ;;  :thread-2-text-1 thread-2-text-1
     :thread-2-header-2 thread-2-header-2
     }))

(generate-ai-responses (:open-ai config/config)
                       {:account-name "Zello"
                        :account-website "https://www.zello.com"
                        :lead-name "Chad Spain"
                        :lead-location "Austin, Texas"
                        :lead-job-title "Account Executive"
                        :seller-name "Archer"
                        :seller-job-title "Account Executive"
                        :seller-company-name "Smartcat"
                        :seller-website "https://smartcat.ai"})

(defn create-sharepage-from-global-template-coordinator [config db organization user {:keys [template-data] :as body}]
  (let [{global-template-id :template-id global-template-organization-id :organization-id} (:global-template config)
        page-data (generate-ai-responses (:open-ai config) template-data)
        sharepage (create-buyersphere-record db (:id organization) (:id user) (assoc body :quick-create-made-by (:seller-name template-data)))
        template-pages (map u/kebab-case (pages/get-buyersphere-active-pages db global-template-organization-id global-template-id))
        template-links (map u/kebab-case (links/get-buyersphere-links db global-template-organization-id global-template-id))]
    (doseq [page template-pages]
      (let [rendered-page (-> page
                              (update-in
                               [:body :sections]
                               (fn [x] (map #(render-section config page-data %) x)))
                              (update :title stache/render page-data))]
        (create-buyersphere-page db (:id organization) (:id sharepage) rendered-page)))
    (links/create-buyersphere-links db (:id organization) (:id sharepage) (map #(update % :title stache/render template-data) template-links))
    sharepage))

(comment
  (create-sharepage-from-global-template-coordinator
   config/config
   db/local-db
   1
   1
   {:buyer "nike"
    :buyer-logo "https://nike.com"
    :template-data {:buyer-first-name "Tom"
                    :seller-first-name "Ryan"
                    :seller-organization "Nike"}})
  ;
  )
