(ns partnorize-api.data.deal-timing
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(def ^:private deal-timing-columns
  [:deal_timing.organization_id :deal_timing.qualified_days
   :deal_timing.evaluation_days :deal_timing.decision_days])

(defn get-deal-timing-by-organization-id [db organization-id]
  (-> (apply h/select deal-timing-columns)
      (h/from :deal_timing)
      (h/where [:= :deal_timing.organization_id organization-id])
      (db/->execute db)
      first))

(defn upsert-deal-timing [db organization-id
                          {:keys [qualified-days evaluation-days decision-days] :as b}]
  (-> (h/insert-into :deal_timing)
      (h/columns :organization_id :qualified_days :evaluation_days :decision_days)
      (h/values [[organization-id qualified-days evaluation-days decision-days]])
      (h/on-conflict :organization_id)
      (h/do-update-set :qualified_days :evaluation_days :decision_days)
      (#(apply h/returning % deal-timing-columns))
      (db/->execute db)
      first))

(comment
  (get-deal-timing-by-organization-id db/local-db 1)
  (upsert-deal-timing db/local-db 1 {:qualified-days 15 :evaluation-days 30 :decision-days 45})
  ;
  )