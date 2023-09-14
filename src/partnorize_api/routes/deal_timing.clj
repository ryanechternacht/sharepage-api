(ns partnorize-api.routes.deal-timing
  (:require [compojure.core :as cpj]
            [partnorize-api.data.deal-timing :as d-deal-timing]
            [partnorize-api.data.permission :as d-permission]
            [ring.util.http-response :as response]))

(def GET-deal-timing
  (cpj/GET "/v0.1/deal-timing" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-deal-timing/get-deal-timing-by-organization-id db (:id organization)))
      (response/unauthorized))))

(def PUT-deal-timing
  (cpj/PUT "/v0.1/deal-timing" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-deal-timing/upsert-deal-timing db (:id organization) body))
      (response/unauthorized))))
