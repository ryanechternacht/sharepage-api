(ns partnorize-api.routes.buyerspheres
  (:require [compojure.core :refer [GET]]
            [compojure.coercions :refer [as-int]]
            [ring.util.response :refer [response]]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]))

(def GET-buyerspheres
  (GET "/v0.1/buyerspheres/:id" [id :<< as-int :as {:keys [db]}]
    (response (d-buyerspheres/get-full-buyersphere db id))))

;; (def GET-obstacles
;;   (GET "/v0.1/student/:student-id/obstacles"
;;     [student-id :<< as-int :as {:keys [user db language]}]
;;     (if (auth/has-student-access? db user student-id :read)
;;       (response (d-obstacles/get-by-student-id db language student-id))
;;       auth/unauthorized-response)))
