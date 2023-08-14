(ns partnorize-api.routes.personas
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [ring.util.http-response :as response]
            [partnorize-api.data.personas :as d-personas]))

;; TODO find a way to automate org-id and user checks
(def GET-personas
  (cpj/GET "/v0.1/personas" {:keys [db user organization]}
    (if user
      (response/ok (d-personas/get-personas-by-organization-id db (:id organization)))
      (response/unauthorized))))

(def POST-personas
  (cpj/POST "/v0.1/personas" {:keys [db user organization body]}
    (if user
      (response/ok (d-personas/create-persona db
                                              (:id organization)
                                              body))
      (response/unauthorized))))

(def PUT-personas
  (cpj/PUT "/v0.1/personas/:id" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if user
      (response/ok (d-personas/update-persona db
                                              (:id organization)
                                              id
                                              body))
      (response/unauthorized))))

;; TODO should we 404 if there isn't one to delete?
(def DELETE-personas
  (cpj/DELETE "/v0.1/personas/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if user
      (response/ok (d-personas/delete-persona db
                                              (:id organization)
                                              id))
      (response/unauthorized))))
