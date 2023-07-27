(ns partnorize-api.routes.features
  (:require [compojure.core :as cpj]
            [ring.util.http-response :as response]
            [partnorize-api.data.features :as d-features]))

;; TODO find a way to automate org-id and user checks
(def GET-features
  (cpj/GET "/v0.1/features" {:keys [db user organization]}
    (if user
      (response/ok (d-features/get-features-by-organization-id db (:id organization)))
      (response/unauthorized))))

;; (def GET-obstacles
;;   (GET "/v0.1/student/:student-id/obstacles"
;;     [student-id :<< as-int :as {:keys [user db language]}]
;;     (if (auth/has-student-access? db user student-id :read)
;;       (response (d-obstacles/get-by-student-id db language student-id))
;;       auth/unauthorized-response)))