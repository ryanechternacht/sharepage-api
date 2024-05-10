(ns partnorize-api.routes.buyer-sessions
   (:require [compojure.core :as cpj]
            [partnorize-api.data.buyer-session :as d-buyer-session]
            [partnorize-api.data.permission :as d-permission]
            [ring.util.http-response :as response]))

(def GET-buyer-sessions
  (cpj/GET "/v0.1/buyer-sessions" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-buyer-session/get-swaypage-sessions db (:id organization)))
      (response/unauthorized))))
