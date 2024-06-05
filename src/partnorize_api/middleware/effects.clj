(ns partnorize-api.middleware.effects
  (:require [partnorize-api.data.buyerspheres :as bs]
            [partnorize-api.db :as db]))

;; (defmulti service-charge (juxt account-level :tag))

;; ;; Handlers for resulting dispatch values
;; (defmethod service-charge [::acc/Basic   ::acc/Checking] [_] 25)

(defmulti handle-db-effect (fn [db thing]
                             (println "db" db)
                             (println "thing" thing)
                             (:target thing)))

(defmethod handle-db-effect [:swaypage :update]
  [db {{:keys [organization-id id] :as swaypage} :value}]
  (println "swaypage update" db swaypage organization-id id)
  (bs/update-buyersphere db organization-id id swaypage))

(defmethod handle-db-effect [:swaypage :delete]
  [db value]
  (println "swaypage delete" db value))

(defn- wrap-effects-impl [handler request]
  (let [{:keys [effects db] :as response} (handler request)]
    (println "effects" effects)
    (doseq [db-effect (:db effects)]
      (println "effect" (:target db-effect) db-effect)
      (handle-db-effect db db-effect))
    (-> response
        (dissoc :db)
        (dissoc :effects))))

(defn wrap-effects [h] (partial #'wrap-effects-impl h))
