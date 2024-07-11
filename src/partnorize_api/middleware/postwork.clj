(ns partnorize-api.middleware.postwork
  (:require [cljstache.core :as stache]
            [honey.sql.helpers :as h]
            [partnorize-api.data.buyerspheres :as bs]
            [partnorize-api.data.buyersphere-pages :as pages]
            [partnorize-api.data.campaigns :as campaigns]
            [partnorize-api.db :as db]
            [partnorize-api.middleware.config :as config]
            [partnorize-api.external-api.open-ai :as open-ai]
            [partnorize-api.data.utilities :as u]))

#_{:clj-kondo/ignore [:unused-binding]}
(defmulti handle-postwork
  (fn [req [[item action id] value]]
    [item action]))

(defn- wrap-postwork-impl [handler req]
  (let [{:keys [postwork] :as res} (handler req)]
    (doseq [pw postwork]
      (handle-postwork req pw))
    res))

(defn wrap-postwork [h] (partial #'wrap-postwork-impl h))

(defmethod handle-postwork [:swaypage :update]
  [{:keys [db organization]} [[_ _ id] changes]]
  (bs/update-buyersphere db (:id organization) id changes))

(comment
  (handle-postwork {:db 123} [[:swaypage :update 1] {:id 1 :c :d}])
  ;
  )

;; TODO instead of create/update would I rather just do upsert/put
;; and have the client update the data as desired, then just
;; have the route do an insert on duplicate update?
;; has the advantage of it's eaiser to return the data?

(defmethod handle-postwork [:csv-upload :create]
  [{:keys [db]} [_ row]]
  (let [formatted-row (-> row
                          (update :header-row db/lift)
                          (update :data-rows db/lift)
                          (update :sample-rows db/lift))
        query (-> (h/insert-into :csv_upload)
                  (h/values [formatted-row]))]
    (db/execute db query)))

(defmethod handle-postwork [:campaign :create]
  [{:keys [db]} [_ row]]
  (let [query (-> (h/insert-into :campaign)
                  (h/values [row]))]
    (db/execute db query)))

(defmethod handle-postwork [:campaign :update]
  [{:keys [db organization]} [[_ _ uuid] updates]]
  (let [fields (select-keys updates [:title
                                     :columns-approved
                                     :ai-prompts-approved
                                     :is-published])
        query (-> (h/update :campaign)
                  (h/set fields)
                  (h/where [:= :organization_id (:id organization)]
                           [:= :uuid uuid]))]
    (db/execute db query)))

(defn- build-logo-url
  "uses the domain provided by the csv row to build a clearbit
   logo api"
  [[_ _ _ _ domain]]
  (str "https://logo.clearbit.com/" domain))

(defmethod handle-postwork [:campaign :publish]
  [{:keys [config db organization user]} [[_ _ uuid] _]]
  (let [{:keys [swaypage_template_id data_rows]}
        (campaigns/get-publish-data db (:id organization) uuid)
        pages (pages/get-buyersphere-pages db (:id organization) swaypage_template_id)
        ai-blocks (reduce
                   (fn [acc {:keys [id body]}]
                     (reduce
                      (fn [acc2 {:keys [type key prompt]}]
                        (if (= type "ai-prompt-template")
                          (assoc acc2 [id key] prompt)
                          acc2))
                      acc
                      (:sections body)))
                   {}
                   pages)]
    ;; TODO we need to create the nano-id in the route, not here
    (doseq [row data_rows]
      (let [page-data (-> (campaigns/reformat-csv-row-for-template row)
                          (assoc :buyer-logo (build-logo-url row)))
            page-data-with-ai
            (reduce (fn [acc [[page-id key] prompt]]
                      (assoc-in acc
                                [:ai page-id key]
                                (open-ai/generate-message (:open-ai config)
                                                          (stache/render prompt page-data))))
                    page-data
                    ai-blocks)]
        (campaigns/create-virtual-swaypage db
                                           (:id organization)
                                           (:id user)
                                           uuid
                                           (u/get-nano-id 7)
                                           page-data-with-ai)))))

(comment
  (handle-postwork {:db db/local-db
                    :config config/config
                    :organization {:id 1}}
                   [[:campaign :publish (java.util.UUID/fromString "0190a024-f0f6-7aca-8eae-a86446d108d5")]])
  ;
  )
