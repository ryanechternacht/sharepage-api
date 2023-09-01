(ns partnorize-api.routes.buyerspheres
  (:require [compojure.core :as cpj]
            [compojure.coercions :as coerce]
            [ring.util.http-response :as response]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.data.conversations :as d-conversations]))

;; TODO find a way to automate org-id and user checks
(def GET-buyerspheres
  (cpj/GET "/v0.1/buyerspheres" [user-id stage :as {:keys [db user organization] :as req}]
    (if user
      (response/ok (d-buyerspheres/get-by-organization db
                                                       (:id organization)
                                                       {:user-id (coerce/as-int user-id)
                                                        :stage stage}))
      (response/unauthorized))))

(def GET-buyersphere
  (cpj/GET "/v0.1/buyerspheres/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if user
      (response/ok (d-buyerspheres/get-full-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def PATCH-buyersphere
  (cpj/PATCH "/v0.1/buyerspheres/:id" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if user
      (response/ok (d-buyerspheres/update-buyersphere db (:id organization) id body))
      (response/unauthorized))))

(def GET-buyersphere-conversations
  (cpj/GET "/v0.1/buyerspheres/:id/conversations" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if user
      (response/ok (d-conversations/get-by-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def POST-buyersphere-conversations
  (cpj/POST "/v0.1/buyerspheres/:id/conversations" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if user
      (response/ok (d-conversations/create-conversation db
                                                        (:id organization)
                                                        id
                                                        (:id user)
                                                        (:message body)))
      (response/unauthorized))))

(def PATCH-buyersphere-conversation
  (cpj/PATCH "/v0.1/buyerspheres/:b-id/conversations/:c-id" 
    [b-id :<< coerce/as-int c-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if user
      (response/ok (d-conversations/update-conversation db
                                                        (:id organization)
                                                        b-id
                                                        c-id
                                                        body))
      (response/unauthorized))))