(ns partnorize-api.routes.buyerspheres
  (:require [compojure.core :as cpj]
            [compojure.coercions :as coerce]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.data.buyersphere-resources :as d-buyer-res]
            [partnorize-api.data.conversations :as d-conversations]
            [ring.util.http-response :as response]))

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

(def POST-buyersphere-resource
  (cpj/POST "/v0.1/buyerspheres/:b-id/resources"
    [b-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if user
      (response/ok (d-buyer-res/create-buyersphere-resource db 
                                                            (:id organization)
                                                            b-id
                                                            body))
      (response/unauthorized))))

(def PATCH-buyersphere-resource
  (cpj/PATCH "/v0.1/buyerspheres/:b-id/resources/:r-id"
    [b-id :<< coerce/as-int r-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if user
      (response/ok (d-buyer-res/update-buyersphere-resource db
                                                            (:id organization)
                                                            b-id
                                                            r-id
                                                            body))
      (response/unauthorized))))

(def DELETE-buyersphere-resource
  (cpj/DELETE "/v0.1/buyerspheres/:b-id/resources/:r-id"
    [b-id :<< coerce/as-int r-id :<< coerce/as-int :as {:keys [db user organization]}]
    (if user
      (response/ok (d-buyer-res/delete-buyersphere-resource db
                                                            (:id organization)
                                                            b-id
                                                            r-id))
      (response/unauthorized))))