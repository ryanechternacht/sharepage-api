(ns partnorize-api.data.pricing
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.db :as db]))

(def ^:private pricing-tier-columns
  [:pricing_tier.id :pricing_tier.organization_id :pricing_tier.ordering
   :pricing_tier.title :pricing_tier.description :pricing_tier.best_for
   :pricing_tier.amount_per_period :pricing_tier.amount_other
   :pricing_tier.period_type])

(defn get-pricing-tiers-by-organization-id [db organization-id]
  (-> (apply h/select pricing-tier-columns)
      (h/from :pricing_tier)
      (h/where [:= :pricing_tier.organization_id organization-id])
      (h/order-by :pricing_tier.ordering)
      (db/->execute db)))

(defn create-pricing-tier [db organization-id
                           {:keys [title description best-for
                                   amount-per-period amount-other period-type]}]
  (-> (h/insert-into :pricing_tier)
      (h/columns :organization_id :ordering :title :description :best_for
                 :amount_per_period :amount_other :period_type)
      (h/values [[organization-id
                  (u/get-next-ordering-query :pricing_tier organization-id)
                  title
                  (u/sanitize-html description)
                  best-for
                  amount-per-period
                  amount-other
                  period-type]])
      (#(apply h/returning % pricing-tier-columns))
      (db/->execute db)
      first))

(defn update-pricing-tier [db organization-id id
                           {:keys [title description best-for
                                   amount-per-period amount-other period-type]}]
  (-> (h/update :pricing_tier)
      (h/set {:title title :description description :best_for best-for
              :amount_per_period amount-per-period :amount_other amount-other
              :period_type period-type})
      (h/where [:= :organization_id organization-id]
               [:= :id id])
      (#(apply h/returning % pricing-tier-columns))
      (db/->execute db)
      first))

(defn delete-pricing-tier [db organization-id id]
  (-> (h/delete-from :pricing_tier)
      (h/where [:= :organization_id organization-id]
               [:= :id id])
      (db/->execute db)))

(comment
  (get-pricing-tiers-by-organization-id db/local-db 1)
  (create-pricing-tier db/local-db 1 {:title "ryan" :description "echternacht"
                                      :best-for "best_for" :amount-per-period 10
                                      :amount-other "hello" :period-type "monthly"})
  (update-pricing-tier db/local-db 1 5 {:title "ryan 2" :description "echternacht 2"
                                        :best-for "best_for_2" :amount-per-period 100
                                        :amount-other "hello 2" :period-type "other"})
  (delete-pricing-tier db/local-db 1 5)
  ;
  )

(defn get-global-pricing-by-organization-id [db organization-id]
  (-> (h/select :organization_id :show_by_default)
      (h/from :pricing_global_settings)
      (h/where [:= :pricing_global_settings.organization_id organization-id])
      (db/->execute db)))

(defn update-global-pricing [db organization-id show-by-default]
  (-> (h/update :pricing_global_settings)
      (h/set {:show_by_default show-by-default})
      (h/where [:= :pricing_global_settings.organization_id organization-id])
      (db/->execute db)))

(comment
  (get-global-pricing-by-organization-id db/local-db 1)
  (update-global-pricing db/local-db 1 false)
  ;
  )