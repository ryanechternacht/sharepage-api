(ns partnorize-api.data.orbits
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.questions :as questions]))

(def ^:private base-orbit-query
  (-> (h/select :orbit.id :orbit.name :orbit.organization_id
                :orbit.status :orbit.logo)
      (h/from :orbit)))

(defn get-orbits-by-id [db ids]
  (-> base-orbit-query
      (h/where [:in :orbit.id ids])
      (db/->execute db)))

(defn get-orbit-by-id [db id]
  (first (get-orbits-by-id db [id])))

(defn get-orbits-by-organization [db organization-id]
  (-> base-orbit-query
      (h/where [:= :orbit.organization_id organization-id])
      ;; TODO what to order on?
      (h/order-by :orbit.name)
      (db/->execute db)))

;; TODO is this how I want this to work?
(defn get-full-orbit [db id]
  (let [orbit (get-orbit-by-id db id)
        qs (questions/get-questions-by-orbit db id)
        qs-grouped-sorted (update-vals (group-by :page qs) #(hash-map :questions (sort-by :ordering %)))]
    (assoc orbit :pages qs-grouped-sorted)))

(comment
  (get-orbit-by-id db/local-db 1)
  (get-orbits-by-organization db/local-db 1)
  (get-full-orbit db/local-db 1)
  ;
  )
