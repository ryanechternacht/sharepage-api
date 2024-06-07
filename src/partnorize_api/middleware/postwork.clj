(ns partnorize-api.middleware.postwork
  (:require [honey.sql.helpers :as h]
            [partnorize-api.data.buyerspheres :as bs]
            [partnorize-api.db :as db]
            [honey.sql :as sql]))

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

(defmethod handle-postwork [:csv-upload :create]
  [{:keys [db]} [[_ _ _] row]]
  (let [query (-> (h/insert-into :csv_upload)
                  (h/values [row]))]
    (db/execute db query)))

(defmethod handle-postwork [:csv-upload-rows :create]
  [{:keys [db]} [[_ _ _] rows]]
  (let [query (-> (h/insert-into :csv_upload_row)
                  (h/values (map #(update % :row-data db/lift) rows)))]
    (db/execute db query)))

(comment
  ;; (handle-postwork {:db 123} [[:swaypage :update 1] {:id 1 :c :d}])
  ;
  )
