(ns partnorize-api.routes.pricing
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.pricing :as d-pricing]
            [partnorize-api.data.permission :as d-permission]
            [ring.util.http-response :as response]))

(def GET-pricing
  (cpj/GET "/v0.1/pricing" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok {:pricing-tiers (d-pricing/get-pricing-tiers-by-organization-id db (:id organization))
                    :settings (d-pricing/get-global-pricing-by-organization-id db (:id organization))})
      (response/unauthorized))))

(def GET-pricing-for-buyersphere
  (cpj/GET "/v0.1/pricing/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/is-buyersphere-visible? db organization id user)
      (response/ok {:pricing-tiers (d-pricing/get-pricing-tiers-by-organization-id db (:id organization))
                    :settings (d-pricing/get-global-pricing-by-organization-id db (:id organization))})
      (response/unauthorized))))

(def PUT-pricing
  (cpj/PUT "/v0.1/pricing" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pricing/upsert-global-pricing db (:id organization) (:show-by-default body)))
      (response/unauthorized))))

(def POST-pricing-tiers
  (cpj/POST "/v0.1/pricing-tiers" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pricing/create-pricing-tier db
                                                  (:id organization)
                                                  body))
      (response/unauthorized))))

(def PUT-pricing-tiers
  (cpj/PUT "/v0.1/pricing-tiers/:id" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pricing/update-pricing-tier db
                                                  (:id organization)
                                                  id
                                                  body))
      (response/unauthorized))))

;; TODO should we 404 if there isn't one to delete?
(def DELETE-pricing-tiers
  (cpj/DELETE "/v0.1/pricing-tiers/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pricing/delete-pricing-tier db
                                                  (:id organization)
                                                  id))
      (response/unauthorized))))
