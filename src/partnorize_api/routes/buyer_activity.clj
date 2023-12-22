(ns partnorize-api.routes.buyer-activity
  (:require [compojure.core :as cpj]
            [partnorize-api.data.buyer-tracking :as d-buyer-tracking]
            [partnorize-api.data.permission :as d-permission]
            [ring.util.http-response :as response]))

(def GET-buyer-activity
  (cpj/GET "/v0.1/buyer-activity" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-buyer-tracking/get-tracking-for-organization db (:id organization)))
      (response/unauthorized))))
