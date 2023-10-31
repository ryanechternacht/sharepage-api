(ns partnorize-api.routes.salesforce
  (:require [compojure.core :as cpj]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.data.salesforce-access :as d-sf]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.external-api.salesforce :as sf]
            [ring.util.http-response :as response]))

;; TODO any/all auth
(def GET-opportunities
  (cpj/GET "/v0.1/salesforce/opportunities" [name :as {:keys [db organization user]}]
    (let [{:keys [instance_url access_token]} (d-sf/get-salesforce-access-details db (:id organization) (:id user))]
      (if-let [opptys (sf/query-opportunities instance_url access_token name)]
        (let [oppty-ids (map :id opptys)
              buyerspheres (d-buyerspheres/get-by-opportunity-ids db (:id organization) oppty-ids)
              bs-by-crm-id (u/index-by :crm_opportunity_id :id buyerspheres)
              opptys-with-buyerspheres
              (map #(assoc % :buyersphere-id (bs-by-crm-id (:id %))) opptys)]
          (response/ok opptys-with-buyerspheres))
        (response/unauthorized)))))
