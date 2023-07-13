(ns partnorize-api.routes.pain-points
  (:require [compojure.core :refer [GET]]
            [ring.util.http-response :as response]
            [partnorize-api.data.pain-points :as d-pain-points]))

;; TODO find a way to automate org-id and user checks
(def GET-pain-points
  (GET "/v0.1/pain-points" {:keys [db user organization]}
    (if user
      (response/ok (d-pain-points/get-pain-points-by-organization-id db (:id organization)))
      (response/unauthorized))))

;; (def GET-obstacles
;;   (GET "/v0.1/student/:student-id/obstacles"
;;     [student-id :<< as-int :as {:keys [user db language]}]
;;     (if (auth/has-student-access? db user student-id :read)
;;       (response (d-obstacles/get-by-student-id db language student-id))
;;       auth/unauthorized-response)))
