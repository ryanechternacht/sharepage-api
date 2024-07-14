(ns partnorize-api.data.virtual-swaypages
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn get-virtual-swaypage-by-shortcode [db organization-id shortcode]
  (let [query (-> (h/select :campaign.swaypage_template_id
                            :virtual_swaypage.page_data
                            :virtual_swaypage.owner_id)
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

(defn get-virtual-swaypages-by-campaign [db organization-id campaign-uuid]
  (let [query (-> (h/select :virtual_swaypage.page_data
                            :virtual_swaypage.owner_id
                            :virtual_swaypage.shortcode)
                   (h/from :virtual_swaypage)
                   (h/where [:and
                             [:= :virtual_swaypage.organization_id organization-id]
                             [:= :virtual_swaypage.campaign_uuid campaign-uuid]]))]
     (->> query
          (db/execute db))))

(comment
  (get-virtual-swaypages-by-campaign db/local-db 1 (java.util.UUID/fromString "0190b271-66bd-7aeb-9427-b4ef20b24000"))
  ;
  )