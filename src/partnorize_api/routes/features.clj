(ns partnorize-api.routes.features
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.features :as d-features]
            [ring.util.http-response :as response]))

;; TODO find a way to automate org-id and user checks
(def GET-features
  (cpj/GET "/v0.1/features" {:keys [db user organization]}
    (if user
      (response/ok (d-features/get-features-by-organization-id db (:id organization)))
      (response/unauthorized))))

(def POST-features
  (cpj/POST "/v0.1/features" {:keys [db user organization body]}
    (if user
      (response/ok (d-features/create-feature db
                                              (:id organization)
                                              body))
      (response/unauthorized))))

(def PUT-features
  (cpj/PUT "/v0.1/features/:id" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if user
      (response/ok (d-features/update-feature db
                                              (:id organization)
                                              id
                                              body))
      (response/unauthorized))))

;; TODO should we 404 if there isn't one to delete?
(def DELETE-features
  (cpj/DELETE "/v0.1/features/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if user
      (response/ok (d-features/delete-feature db
                                              (:id organization)
                                              id))
      (response/unauthorized))))
