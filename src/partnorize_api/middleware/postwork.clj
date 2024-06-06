(ns partnorize-api.middleware.postwork
  (:require [partnorize-api.data.buyerspheres :as bs]
            [partnorize-api.db :as db]))

(defmulti handle-postwork (fn [req [[item action id] value]]
                             [item action]))

(defmethod handle-postwork [:swaypage :update] 
  [{:keys [db organization]} [[_ _ id] changes]]
  (bs/update-buyersphere db (:id organization) id changes))

(comment
  (handle-postwork {:db 123} [[:swaypage :update 1] {:id 1 :c :d}])
  ;
  )

(defn- wrap-postwork-impl [handler req]
  (let [{:keys [postwork] :as res} (handler req)]
    (doseq [pw postwork]
      (handle-postwork req pw))
    res))

(defn wrap-postwork [h] (partial #'wrap-postwork-impl h))
