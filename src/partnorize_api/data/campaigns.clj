(ns partnorize-api.data.campaigns
  (:require  [honey.sql.helpers :as h]
             [partnorize-api.db :as db]
             [partnorize-api.data.buyerspheres :as bs]
             [partnorize-api.data.utilities :as u]))

(defn get-by-uuid [db organization-id uuid]
  (let [query (-> (h/select :campaign.uuid
                            :campaign.organization_id
                            :campaign.title
                            :campaign.columns_approved
                            :campaign.ai_prompts_approved
                            :campaign.is_published
                            :campaign.swaypage_template_id
                            :csv_upload.sample_rows
                            :csv_upload.header_row)
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
        (update :uuid u/uuid->friendly-id))))

(comment
  (get-by-uuid db/local-db
                1
                (java.util.UUID/fromString "01900a3d-80c9-78c9-9a2c-37ceec4e1c93"))
  ;
  )
