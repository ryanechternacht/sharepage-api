(ns partnorize-api.data.campaigns
  (:require  [honey.sql.helpers :as h]
             [partnorize-api.db :as db]
             [partnorize-api.data.buyerspheres :as bs]
             [partnorize-api.data.utilities :as u]
             [partnorize-api.middleware.config :as config]))

(defn- reformat-csv-upload
  [{:keys [file_name sample_rows header_row data_rows_count] :as row}]
  (-> row
      (assoc :leads-file {:file-name file_name
                          :sample-rows sample_rows
                          :header-row header_row
                          :data-rows-count data_rows_count})
      (dissoc :file_name)
      (dissoc :sample_rows)
      (dissoc :header_row)
      (dissoc :data_rows_count)))

(defn get-by-uuid [db organization-id uuid]
  (let [query (-> (h/select :campaign.uuid
                            :campaign.organization_id
                            :campaign.title
                            :campaign.columns_approved
                            :campaign.ai_prompts_approved
                            :campaign.is_published
                            :campaign.swaypage_template_id
                            :csv_upload.file_name
                            :csv_upload.sample_rows
                            :csv_upload.header_row
                            :csv_upload.data_rows_count)
                  (h/from :campaign)
                  (h/join :csv_upload [:= :campaign.csv_upload_uuid :csv_upload.uuid])
                  (h/where [:= :campaign.organization-id organization-id]
                           [:= :campaign.uuid uuid]))
        {:keys [swaypage_template_id] :as campaign}
        (->> query
             (db/execute db)
             first)]
    (-> campaign
        (dissoc :swaypage_template_id)
        (assoc :template (bs/get-full-buyersphere db organization-id swaypage_template_id))
        reformat-csv-upload
        (update :uuid u/uuid->friendly-id))))

(comment
  (get-by-uuid db/local-db
               1
               (java.util.UUID/fromString "01900e0f-9f5e-7b9c-bcc5-98fe4a32a090"))
  ;
  )

;; TODO rename?
(defn reformat-csv-row-for-template
  [[account-name first-name last-name email domain & fields]]
  (reduce-kv (fn [acc i f]
               (assoc acc (keyword (str "field-" (inc i))) f))
             {:account-name account-name
              :first-name first-name
              :last-name last-name
              :email email
              :domain domain}
             (vec fields)))

(defn get-publish-data [db organization-id uuid]
  (let [query (-> (h/select :campaign.swaypage_template_id
                            :csv_upload.data_rows
                            :csv_upload.header_row)
                  (h/from :campaign)
                  (h/join :csv_upload [:= :campaign.csv_upload_uuid :csv_upload.uuid])
                  (h/where [:= :campaign.organization-id organization-id]
                           [:= :campaign.uuid uuid]))]
    (->> query
         (db/execute db)
         first)))

(defn get-all [db organization-id]
  (let [query (-> (h/select :campaign.uuid
                            :campaign.organization_id
                            :campaign.title
                            :campaign.columns_approved
                            :campaign.ai_prompts_approved
                            :campaign.is_published
                            [:csv_upload.data_rows_count :lead_count])
                  (h/from :campaign)
                  (h/join :csv_upload [:= :campaign.csv_upload_uuid :csv_upload.uuid])
                  (h/where [:= :campaign.organization-id organization-id]))]
        (->> query
             (db/execute db)
             (map #(update % :uuid u/uuid->friendly-id)))))

(comment
  (get-all db/local-db 1)
  ;
  )

(defn make-swaypage-link [subdomain domain swaypage-shortcode [_ first-name last-name]]
  (str "https://" subdomain "." domain "/u/" swaypage-shortcode "/" first-name "%20" last-name))

(comment
  (make-swaypage-link "stark" "swaypage.io" "abc123" ["" "ryan" "echternacht"])
  )

(defn get-published-csv [{{domain :domain} :cookie-attrs}
                         db
                         {:keys [id subdomain]}
                         uuid]
  (let [{:keys [data_rows header_row]} (get-publish-data db id uuid)
        swaypages (bs/get-by-organization db id {:campaign-uuid uuid})
        shortcode-by-rownum (reduce (fn [acc {:keys [shortcode campaign_row_number]}]
                                      (assoc acc campaign_row_number shortcode))
                                    {}
                                    swaypages)]
    (apply conj
           [(conj (vec header_row) "Swaypage Link")]
           (map-indexed (fn [i row]
                          (conj row (make-swaypage-link subdomain domain (shortcode-by-rownum i) row)))
                        data_rows))))

(comment
  (get-published-csv config/config
                     db/local-db
                     {:id 1 :subdomain "stark"}
                     (java.util.UUID/fromString "01900e0f-9f5e-7b9c-bcc5-98fe4a32a090"))
  ;
  )

(defn create-virtual-swaypage [db organization-id owner-id campaign-uuid shortcode page-data]
  (let [query (-> (h/insert-into :virtual_swaypage)
                  (h/values [{:organization_id organization-id
                              :owner_id owner-id
                              :campaign_uuid campaign-uuid
                              :shortcode shortcode
                              :page_data [:lift page-data]}]))]
    (->> query
         (db/execute db)
         first)))

(comment
  (create-virtual-swaypage db/local-db
                           1
                           1
                           (java.util.UUID/fromString "01909abe-006e-7e48-b260-85bf31ae08ac")
                           "abc1236"
                           {:first-name "ryan 2"
                            :last-name "echternacht"
                            :account-name "nike"
                            :email "ryan@echternacht.org"
                            :domain "nike.com"
                            :field-1 "some data"
                            :field-2 "other data"
                            :field-3 "more data"
                            :ai {3 "hello"}})
  ;
  )
