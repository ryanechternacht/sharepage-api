(ns partnorize-api.data.buyersphere-templates
  (:require [cljstache.core :as stache]
            [clojure.string :as str]
            [com.climate.claypoole :as cp]
            [honey.sql.helpers :as h]
            [partnorize-api.data.buyerspheres :as buyerspheres]
            [partnorize-api.data.buyersphere-pages :as pages]
            [partnorize-api.data.buyersphere-links :as links]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.db :as db]
            [partnorize-api.external-api.open-ai :as open-ai]
            [partnorize-api.middleware.config :as config]))

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

;; (def context)
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
                         (select-keys [:lead-name :lead-job-title :lead-location :account-name :account-website
                                       :seller-name :seller-job-title :seller-company :seller-website]))
        context-prompt (slurp "resources/ai/context-prompt.mustache")
        rendered-context (stache/render context-prompt context-data)
        context (open-ai/generate-message openai-config rendered-context "Generate this response in plain text")

        city-check-fn (fn [] (-> "resources/ai/city-check-prompt.mustache"
                                 slurp
                                 (stache/render context-data)
                                 (#(open-ai/generate-message openai-config % context))
                                 strip-html-response))

        thread-1-header-fn (fn [] (-> "resources/ai/thread-1-header-prompt.mustache"
                                      slurp
                                      (stache/render context-data)
                                      (#(open-ai/generate-message openai-config % context))
                                      strip-html-response))

        thread-1-header-1-fn (fn [] (-> "resources/ai/thread-1-header-1-prompt.mustache"
                                        slurp
                                        (stache/render context-data)
                                        (#(open-ai/generate-message openai-config % context))
                                        strip-html-response))

        thread-1-text-1-fn (fn [] (-> "resources/ai/thread-1-text-1-prompt.mustache"
                                      slurp
                                      (stache/render context-data)
                                      (#(open-ai/generate-message openai-config % context))
                                      strip-html-response))

        thread-1-header-2-fn (fn [] (-> "resources/ai/thread-1-header-2-prompt.mustache"
                                        slurp
                                        (stache/render context-data)
                                        (#(open-ai/generate-message openai-config % context))
                                        strip-html-response))

        thread-1-header-3-fn (fn [] (-> "resources/ai/thread-1-header-3-prompt.mustache"
                                        slurp
                                        (stache/render context-data)
                                        (#(open-ai/generate-message openai-config % context))
                                        strip-html-response))

        thread-1-text-3-fn (fn [] (-> "resources/ai/thread-1-text-3-prompt.mustache"
                                      slurp
                                      (stache/render context-data)
                                      (#(open-ai/generate-message openai-config % context))
                                      strip-html-response))

        thread-1-header-4-fn (fn [] (-> "resources/ai/thread-1-header-4-prompt.mustache"
                                        slurp
                                        (stache/render context-data)
                                        (#(open-ai/generate-message openai-config % context))
                                        strip-html-response))

        ;; thread-1-image-search-term (-> "resources/ai/thread-1-image-search-term-prompt.mustache"
        ;;                              slurp
        ;;                              (stache/render context-data)
        ;;                              (#(open-ai/generate-message openai-config % context))
        ;;                              strip-html-response)

        thread-2-header-fn (fn [] (-> "resources/ai/thread-2-header-prompt.mustache"
                                      slurp
                                      (stache/render context-data)
                                      (#(open-ai/generate-message openai-config % context))
                                      strip-html-response))

        thread-2-header-1-fn (fn [] (-> "resources/ai/thread-2-header-1-prompt.mustache"
                                        slurp
                                        (stache/render context-data)
                                        (#(open-ai/generate-message openai-config % context))
                                        strip-html-response))

        thread-2-text-1-fn (fn [] (-> "resources/ai/thread-2-text-1-prompt.mustache"
                                      slurp
                                      (stache/render context-data)
                                      (#(open-ai/generate-message openai-config % context))
                                      strip-html-response))

        thread-2-header-2-fn (fn [] (-> "resources/ai/thread-2-header-2-prompt.mustache"
                                        slurp
                                        (stache/render context-data)
                                        (#(open-ai/generate-message openai-config % context))
                                        strip-html-response))

        thread-3-body-fn (fn [] (-> "resources/ai/thread-3-body-prompt.mustache"
                                    slurp
                                    (stache/render context-data)
                                    (#(open-ai/generate-message openai-config % context))
                                    strip-html-response))

        round-1 (reduce into
                        {}
                        (cp/pmap 100
                        ;; (map
                                 (fn [[kw f]]
                                   {kw (f)})
                                 {:city-check city-check-fn
                                  :thread-1-header thread-1-header-fn
                                  :thread-1-header-1 thread-1-header-1-fn
                                  :thread-1-text-1 thread-1-text-1-fn
                                  :thread-1-header-2 thread-1-header-2-fn
                                  :thread-1-header-3 thread-1-header-3-fn
                                  :thread-1-text-3 thread-1-text-3-fn
                                  :thread-1-header-4 thread-1-header-4-fn
                                  :thread-2-header thread-2-header-fn
                                  :thread-2-header-1 thread-2-header-1-fn
                                  :thread-2-text-1 thread-2-text-1-fn
                                  :thread-2-header-2 thread-2-header-2-fn
                                  :thread-3-body thread-3-body-fn}))

        context-data+round-1 (conj context-data round-1)

        thread-1-subtext-fn (fn [] (-> "resources/ai/thread-1-subtext-prompt.mustache"
                                       slurp
                                       (stache/render context-data+round-1)
                                       (#(open-ai/generate-message openai-config % context))
                                       strip-html-response))

        thread-2-subtext-fn (fn [] (-> "resources/ai/thread-2-header-prompt.mustache"
                                       slurp
                                       (stache/render context-data+round-1)
                                       (#(open-ai/generate-message openai-config % context))
                                       strip-html-response))

        round-2 (reduce into
                        round-1
                        (cp/pmap 100
                                        ;; (map
                                 (fn [[kw f]]
                                   {kw (f)})
                                 {:thread-1-subtext thread-1-subtext-fn
                                  :thread-2-subtext thread-2-subtext-fn}))]

    ;; {
    ;;  :thread-1-header thread-1-header
    ;;  :thread-1-subtext thread-1-subtext
    ;;  :thread-1-header-1 thread-1-header-1
    ;;  :thread-1-text-1 thread-1-text-1
    ;;  :thread-1-header-2 thread-1-header-2
    ;;  :thread-1-header-3 thread-1-header-3
    ;;  :thread-1-text-3 thread-1-text-3
    ;;  :thread-1-header-4 thread-1-header-4

    ;; ;; TODO
    ;; ;;  :thread-1-image-search-term thread-1-image-search-term
     
    ;;  :thread-2-header thread-2-header 
    ;;  :thread-2-subtext thread-2-subtext
    ;;  :thread-2-header-1 thread-2-header-1
    ;;  :thread-2-text-1 thread-2-text-1
    ;;  :thread-2-header-2 thread-2-header-2
    ;;  :thread-3-body thread-3-body
    ;;  }
    round-2))

;; (reduce into {} (pmap (fn [x] {(keyword (str x)) x}) (range 5)))

(def ^:private location-to-image-map 
  {"Atlanta" {:url "https://images.unsplash.com/photo-1575917649705-5b59aaa12e6b?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODc1MDh8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/brxdlxy" :name "Brad Huchteman"} :blurhash "LkK9=k-ms.t6^-xZayj[0*RlWBWV"}
   "Austin" {:url "https://images.unsplash.com/photo-1588993608283-7f0eda4438be?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfHNlYXJjaHw2fHxhdXN0aW58ZW58MHwwfHx8MTcyNDM4Mjg4NHww&ixlib=rb-4.0.3&q=85&w=2400", :author {"link" "https://api.unsplash.com/users/mitchkmetz", "name" "Mitchell Kmetz"}, "blurhash" "LsG[_~WCbakD?woJoLs:p0t6WXj?"}
   "Boston" {:url "https://plus.unsplash.com/premium_photo-1694475434235-12413ec38b3e?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODMyMzV8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/gettyimages" :name "Getty Images"} :blurhash "LVI#iT-pIoNe~qxbWBR+lUkDRPae"}
   "Chicago" {:url "https://images.unsplash.com/photo-1547838555-1a3b10c67181?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODM0MjJ8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/gautamkrishnan" :name "Gautam Krishnan"} :blurhash "LdJa4dR+NGWC~VoKaej[Xo$%WVoL"}
   "Dallas" {:url "https://images.unsplash.com/photo-1631660975301-b2b65e80c98a?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODcyNzB8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/thmxfy" :name "Max Fray"} :blurhash "LN9%Sss;W=Wq-soMj]ju-@j[j[fl"}
   "Denver" {:url "https:https://images.unsplash.com/photo-1648441095877-90406e6ba04d?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODM1NTV8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/billgrip" :name "Bill Griepenstroh"} :blurhash "LaB4zWV@RjofT}aeoJayELofoeWC"}
   "Houston" {:url "https://images.unsplash.com/photo-1692154600992-463fa9b27abd?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODc0MjB8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/jeswinthomas" :name "Jeswin Thomas"} :blurhash "LD5YTtZ#fhs:YRivs,bHO [aJjYR+"}
   "Los Angeles" {:url "https://images.unsplash.com/photo-1590397883410-ddccd55ef3d2?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODM2NzZ8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/alessguarino" :name "Alessandro Guarino"} :blurhash "LqHeRWxaj[oe~qs:oLj[S$a}WVay"}
   "Miami" {:url "https://images.unsplash.com/photo-1605723517503-3cadb5818a0c?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODczNTR8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/dennycshots" :name "Denys Kostyuchenko"} :blurhash "LUGI.dD%ofozOujZWBkC0fjYjsj@"}
   "New York" {:url "https://images.unsplash.com/photo-1500228630616-d6905f33ad1a?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODY4MTJ8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/mohitsingh1691" :name "Mohit Singh"} :blurhash "LnK^$}sR%2t6~Wslt6oJ4:ofofof"}
   "San Francisco" {:url "https://images.unsplash.com/photo-1573878539923-7fc5642ebee3?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODcxNDh8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/nietramos_d" :name "DAVID NIETO"} :blurhash "LfHVPEofR*e.?wayj[kCR+WBj[a#"}
   "Seattle" {:url "https://images.unsplash.com/photo-1543364074-4055b532c5d8?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODY5ODZ8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/mbicca" :name "Marco Bicca"} :blurhash "LLBDypW?MwRj.Aj@RiRkRin#ayay"}
   "none" {:url "https://images.unsplash.com/photo-1498429089284-41f8cf3ffd39?crop=entropy&cs=srgb&fm=jpg&ixid=M3w2NDMzMDZ8MHwxfGFsbHx8fHx8fHx8fDE3MjQzODc3MDh8&ixlib=rb-4.0.3&q=85&w=2400" :author {:link "https://api.unsplash.com/users/anik3t" :name "Aniket Deole"} :blurhash "LHA13ExZDhIp?wWCZ~WC9bNH%M%1"}})

;; (time
;;  (generate-ai-responses (:open-ai config/config)
;;                         {:account-name "Zello"
;;                          :account-website "https://www.zello.com"
;;                          :lead-name "Chad Spain"
;;                          :lead-location "Austin, Texas"
;;                          :lead-job-title "Account Executive"
;;                          :seller-name "Archer"
;;                          :seller-job-title "Account Executive"
;;                          :seller-company-name "Smartcat"
;;                          :seller-website "https://smartcat.ai"}))

(defn create-sharepage-from-global-template-coordinator [config db organization user {:keys [template-data] :as body}]
  (let [{global-template-id :template-id global-template-organization-id :organization-id} (:global-template config)
        page-data (generate-ai-responses (:open-ai config) template-data)
        sharepage (create-buyersphere-record db (:id organization) (:id user) (assoc body :quick-create-made-by (:seller-name template-data)))
        template-pages (map u/kebab-case (pages/get-buyersphere-active-pages db global-template-organization-id global-template-id))
        template-links (map u/kebab-case (links/get-buyersphere-links db global-template-organization-id global-template-id))
        _ (println "page-data" page-data)
        header-image (location-to-image-map (:city-check page-data))
        _ (println "header-image" header-image)]
    (doseq [page template-pages]
      (let [rendered-page (-> page
                              (update-in
                               [:body :sections]
                               (fn [x] (map #(render-section config page-data %) x)))
                              (update :title stache/render page-data)
                              (assoc :header-image header-image))]
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
