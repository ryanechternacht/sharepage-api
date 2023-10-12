(ns partnorize-api.routes.pain-points
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [ring.util.http-response :as response]
            [partnorize-api.data.pain-points :as d-pain-points]
            [partnorize-api.data.permission :as d-permission]))

;; TODO find a way to automate org-id and user checks
(def GET-pain-points
  (cpj/GET "/v0.1/pain-points" {:keys [db user organization]}
    (if (d-permission/can-user-see-anything? db organization user)
      (response/ok (d-pain-points/get-pain-points-by-organization-id db (:id organization)))
      (response/unauthorized))))

(def POST-pain-points
  (cpj/POST "/v0.1/pain-points" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pain-points/create-pain-point db
                                                    (:id organization)
                                                    body))
      (response/unauthorized))))

(def PUT-pain-points
  (cpj/PUT "/v0.1/pain-points/:id" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pain-points/update-pain-point db
                                                    (:id organization)
                                                    id
                                                    body))
      (response/unauthorized))))

;; TODO should we 404 if there isn't one to delete?
(def DELETE-pain-points
  (cpj/DELETE "/v0.1/pain-points/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-pain-points/delete-pain-point db
                                                    (:id organization)
                                                    id))
      (response/unauthorized))))
