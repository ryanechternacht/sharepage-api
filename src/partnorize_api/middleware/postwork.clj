(ns partnorize-api.middleware.postwork
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.buyerspheres :as bs]
            [partnorize-api.db :as db]))

(defmulti handle-postwork (fn [req [[item action id] value]]
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
