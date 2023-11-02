(ns partnorize-api.routes.salesforce
  (:require [compojure.core :as cpj]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.external-api.salesforce :as sf]
            [ring.util.http-response :as response]))

;; TODO any/all auth
(def GET-opportunities
  (cpj/GET "/v0.1/salesforce/opportunities" [name only-mine :as {:keys [db organization user config]}]
    (let [organization-id (:id organization)]
      (if-let [opptys (sf/query-opportunities-with-sf-refresh! (:salesforce config) db organization-id (:id user) name only-mine)]
        (let [oppty-ids (map :id opptys)
              buyerspheres (d-buyerspheres/get-by-opportunity-ids db organization-id oppty-ids)
              bs-by-crm-id (u/index-by :crm_opportunity_id :id buyerspheres)
              opptys-with-buyerspheres
              (map #(assoc % :buyersphere-id (bs-by-crm-id (:id %))) opptys)]
          (response/ok opptys-with-buyerspheres))
        (response/unauthorized)))))
