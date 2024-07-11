(ns partnorize-api.data.virtual-swaypages
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn get-virtual-swaypage-by-shortcode [db organization-id shortcode]
  (let [query (-> (h/select :campaign.swaypage_template_id
                            :virtual_swaypage.page_data)
                  (h/from :virtual_swaypage)
                  (h/join :campaign [:and
                                     [:= :virtual_swaypage.organization_id :campaign.organization_id]
                                     [:= :virtual_swaypage.campaign_uuid :campaign.uuid]])
                  (h/where [:and
                            [:= :virtual_swaypage.organization_id organization-id]
                            [:= :virtual_swaypage.shortcode shortcode]]))]
    (->> query
         (db/execute db)
         first)))

(comment
  (get-virtual-swaypage-by-shortcode db/local-db 1 "abc1235")
  ;
  )
