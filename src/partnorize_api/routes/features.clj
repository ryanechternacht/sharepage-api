(ns partnorize-api.routes.features
  (:require [compojure.core :as cpj]
            [partnorize-api.data.features :as d-features]
            [ring.util.http-response :as response]))

;; TODO find a way to automate org-id and user checks
(def GET-features
  (cpj/GET "/v0.1/features" {:keys [db user organization]}
    (if user
      (response/ok (d-features/get-features-by-organization-id db (:id organization)))
      (response/unauthorized))))

(def POST-feature
  (cpj/POST "/v0.1/features" {:keys [db user organization body]}
    (if user
      (response/ok (d-features/create-feature db
                                              (:id organization)
                                              body))
      (response/unauthorized))))
