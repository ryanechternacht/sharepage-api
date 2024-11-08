(ns partnorize-api.routes.resources
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.permission :as d-permission]
            [partnorize-api.data.resources :as d-resources]
            [ring.util.http-response :as response]))

;; TODO find a way to automate org-id and user checks
(def GET-resources
  (cpj/GET "/v0.1/resources" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-resources/get-resources-by-organization-id db (:id organization)))
      (response/unauthorized))))

(def POST-resources
  (cpj/POST "/v0.1/resources" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-resources/create-resource db
                                              (:id organization)
                                              body))
      (response/unauthorized))))

(def PUT-resources
  (cpj/PUT "/v0.1/resources/:id" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-resources/update-resource db
                                              (:id organization)
                                              id
                                              body))
      (response/unauthorized))))

;; TODO should we 404 if there isn't one to delete?
(def DELETE-resources
  (cpj/DELETE "/v0.1/resources/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-resources/delete-resource db
                                              (:id organization)
                                              id))
      (response/unauthorized))))