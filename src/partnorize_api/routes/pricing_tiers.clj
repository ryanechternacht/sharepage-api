(ns partnorize-api.routes.pricing-tiers
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.pricing-tiers :as d-pricing-tiers]
            [partnorize-api.data.permission :as d-permission]
            [ring.util.http-response :as response]))

;; TODO find a way to automate org-id and user checks
(def GET-pricing-tiers
  (cpj/GET "/v0.1/pricing-tiers" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pricing-tiers/get-pricing-tiers-by-organization-id db (:id organization)))
      (response/unauthorized))))

(def POST-pricing-tiers
  (cpj/POST "/v0.1/pricing-tiers" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pricing-tiers/create-pricing-tier db
                                              (:id organization)
                                              body))
      (response/unauthorized))))

(def PUT-pricing-tiers
  (cpj/PUT "/v0.1/pricing-tiers/:id" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pricing-tiers/update-pricing-tier db
                                              (:id organization)
                                              id
                                              body))
      (response/unauthorized))))

;; TODO should we 404 if there isn't one to delete?
(def DELETE-pricing-tiers
  (cpj/DELETE "/v0.1/pricing-tiers/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pricing-tiers/delete-pricing-tier db
                                              (:id organization)
                                              id))
      (response/unauthorized))))
