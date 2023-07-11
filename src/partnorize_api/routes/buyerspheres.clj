(ns partnorize-api.routes.buyerspheres
  (:require [compojure.core :refer [GET]]
            [compojure.coercions :refer [as-int]]
            [ring.util.http-response :as response]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]))

;; TODO find a way to automate org-id and user checks
(def GET-buyerspheres
  (GET "/v0.1/buyerspheres/:id" [id :<< as-int :as {:keys [db user organization]}]
    (if user
      (response/ok (d-buyerspheres/get-full-buyersphere db (:id organization) id))
      (response/unauthorized))))

;; (def GET-obstacles
;;   (GET "/v0.1/student/:student-id/obstacles"
;;     [student-id :<< as-int :as {:keys [user db language]}]
;;     (if (auth/has-student-access? db user student-id :read)
;;       (response (d-obstacles/get-by-student-id db language student-id))
;;       auth/unauthorized-response)))
