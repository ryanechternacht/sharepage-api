(ns partnorize-api.routes.personas
  (:require [compojure.core :as cpj]
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
    (println body)
    (if user
      (response/ok (d-personas/create-persona db
                                              (:id organization)
                                              (:persona body)))
      (response/unauthorized))))
