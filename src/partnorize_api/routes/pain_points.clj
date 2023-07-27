(ns partnorize-api.routes.pain-points
  (:require [compojure.core :as cpj]
            [ring.util.http-response :as response]
            [partnorize-api.data.pain-points :as d-pain-points]))

;; TODO find a way to automate org-id and user checks
(def GET-pain-points
  (cpj/GET "/v0.1/pain-points" {:keys [db user organization]}
    (if user
      (response/ok (d-pain-points/get-pain-points-by-organization-id db (:id organization)))
      (response/unauthorized))))

(def POST-pain-points
  (cpj/POST "/v0.1/pain-points" {:keys [db user organization body]}
    (if user
      (response/ok (d-pain-points/create-pain-point db
                                                    (:id organization)
                                                    body))
      (response/unauthorized))))
