(ns partnorize-api.routes.organization
  (:require [compojure.core :as cpj]
            [partnorize-api.data.organizations :as d-organization]
            [partnorize-api.data.permission :as d-permission]
            [ring.util.http-response :as response]))

;; TODO do we need to auth this? probably?
(def GET-organization
  (cpj/GET "/v0.1/organization" {:keys [organization]}
    (if organization
      (response/ok organization)
      (response/not-found))))

(def PATCH-organization
  (cpj/PATCH "/v0.1/organization" {:keys [db organization body user]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-organization/update-organization db (:id organization) body))
      (response/unauthorized))))
