(ns partnorize-api.routes.buyerspheres
  (:require [compojure.core :refer [GET PATCH POST]]
            [compojure.coercions :refer [as-int]]
            [ring.util.http-response :as response]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.data.conversations :as d-conversations]))

;; TODO find a way to automate org-id and user checks
(def GET-buyerspheres
  (GET "/v0.1/buyerspheres/:id" [id :<< as-int :as {:keys [db user organization]}]
    (if user
      (response/ok (d-buyerspheres/get-full-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def PATCH-buyerspheres-features
  (PATCH "/v0.1/buyerspheres/:id/features" [id :<< as-int :as {:keys [db user organization body]}]
    (if user
      (response/ok (d-buyerspheres/save-buyersphere-feature-answer db (:id organization) id body))
      (response/unauthorized))))

(def GET-buyerspheres-conversations
  (GET "/v0.1/buyerspheres/:id/conversations" [id :<< as-int :as {:keys [db user organization]}]
    (if user
      (response/ok (d-conversations/get-by-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def POST-buyerspheres-conversations
  (POST "/v0.1/buyerspheres/:id/conversations" [id :<< as-int :as {:keys [db user organization body]}]
    (if user
      (response/ok (d-conversations/create-conversation db
                                                        (:id organization)
                                                        id
                                                        (:id user)
                                                        (:message body)))
      (response/unauthorized))))

;; (def GET-obstacles
;;   (GET "/v0.1/student/:student-id/obstacles"
;;     [student-id :<< as-int :as {:keys [user db language]}]
;;     (if (auth/has-student-access? db user student-id :read)
;;       (response (d-obstacles/get-by-student-id db language student-id))
;;       auth/unauthorized-response)))
