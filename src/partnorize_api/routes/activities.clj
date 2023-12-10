(ns partnorize-api.routes.activities
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.conversations :as d-conversations]
            [partnorize-api.data.permission :as d-permission]
            [ring.util.http-response :as response]))

(def GET-activities
  (cpj/GET "/v0.1/activities" [user-id :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-conversations/get-by-organization db
                                                        (:id organization)
                                                        {:user-id (coerce/as-int user-id)}))
      (response/unauthorized))))
