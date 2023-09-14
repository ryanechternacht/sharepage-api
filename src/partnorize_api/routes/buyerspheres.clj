(ns partnorize-api.routes.buyerspheres
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.data.buyersphere-notes :as d-buyer-notes]
            [partnorize-api.data.buyersphere-resources :as d-buyer-res]
            [partnorize-api.data.conversations :as d-conversations]
            [partnorize-api.data.permission :as d-permission]
            [partnorize-api.data.users :as d-users]
            [ring.util.http-response :as response]
            [partnorize-api.data.teams :as d-teams]))

;; TODO find a way to automate org-id and user checks
(def GET-buyerspheres
  (cpj/GET "/v0.1/buyerspheres" [user-id stage :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-buyerspheres/get-by-organization db
                                                       (:id organization)
                                                       {:user-id (coerce/as-int user-id)
                                                        :stage stage}))
      (response/unauthorized))))

(def GET-buyersphere
  (cpj/GET "/v0.1/buyerspheres/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (response/ok (d-buyerspheres/get-full-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def POST-buyersphere
  (cpj/POST "/v0.1/buyerspheres" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-buyerspheres/create-buyersphere db organization body))
      (response/unauthorized))))

(def PATCH-buyersphere
  (cpj/PATCH "/v0.1/buyerspheres/:id" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (response/ok (d-buyerspheres/update-buyersphere db (:id organization) id body))
      (response/unauthorized))))

(def GET-buyersphere-conversations
  (cpj/GET "/v0.1/buyerspheres/:id/conversations" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (response/ok (d-conversations/get-by-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def POST-buyersphere-conversations
  (cpj/POST "/v0.1/buyerspheres/:id/conversations" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (response/ok (d-conversations/create-conversation db
                                                        (:id organization)
                                                        id
                                                        (:id user)
                                                        (:message body)))
      (response/unauthorized))))

(def PATCH-buyersphere-conversation
  (cpj/PATCH "/v0.1/buyerspheres/:b-id/conversations/:c-id" 
    [b-id :<< coerce/as-int c-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (response/ok (d-conversations/update-conversation db
                                                        (:id organization)
                                                        b-id
                                                        c-id
                                                        body))
      (response/unauthorized))))

(def POST-buyersphere-resource
  (cpj/POST "/v0.1/buyerspheres/:b-id/resources"
    [b-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (response/ok (d-buyer-res/create-buyersphere-resource db 
                                                            (:id organization)
                                                            b-id
                                                            body))
      (response/unauthorized))))

(def PATCH-buyersphere-resource
  (cpj/PATCH "/v0.1/buyerspheres/:b-id/resources/:r-id"
    [b-id :<< coerce/as-int r-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (response/ok (d-buyer-res/update-buyersphere-resource db
                                                            (:id organization)
                                                            b-id
                                                            r-id
                                                            body))
      (response/unauthorized))))

(def DELETE-buyersphere-resource
  (cpj/DELETE "/v0.1/buyerspheres/:b-id/resources/:r-id"
    [b-id :<< coerce/as-int r-id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (response/ok (d-buyer-res/delete-buyersphere-resource db
                                                            (:id organization)
                                                            b-id
                                                            r-id))
      (response/unauthorized))))

(def POST-buyersphere-note
  (cpj/POST "/v0.1/buyerspheres/:b-id/notes"
    [b-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (response/ok (d-buyer-notes/create-buyersphere-note db
                                                          (:id organization)
                                                          b-id
                                                          body))
      (response/unauthorized))))

(def PATCH-buyersphere-note
  (cpj/PATCH "/v0.1/buyerspheres/:b-id/notes/:n-id"
    [b-id :<< coerce/as-int n-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (response/ok (d-buyer-notes/update-buyersphere-note db
                                                          (:id organization)
                                                          b-id
                                                          n-id
                                                          body))
      (response/unauthorized))))

(def DELETE-buyersphere-note
  (cpj/DELETE "/v0.1/buyerspheres/:b-id/notes/:n-id"
    [b-id :<< coerce/as-int n-id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (response/ok (d-buyer-notes/delete-buyersphere-note db
                                                          (:id organization)
                                                          b-id
                                                          n-id))
      (response/unauthorized))))

(def POST-add-buyer-to-buyersphere
  (cpj/POST "/v0.1/buyerspheres/:id/teams/buyer"
    [id :<< coerce/as-int :as {:keys [config db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (let [new-user (d-users/create-user config
                                          db
                                          organization
                                          body)
            _ (d-teams/add-user-to-buyersphere db
                                               (:id organization)
                                               id
                                               "buyer"
                                                (:id new-user))]
        (response/ok (:buyer-team (d-teams/get-by-buyersphere db
                                                              (:id organization)
                                                              id))))
      (response/unauthorized))))

(def POST-add-seller-to-buyersphere
  (cpj/POST "/v0.1/buyerspheres/:id/teams/seller"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (let [_ (d-teams/add-user-to-buyersphere db
                                               (:id organization)
                                               id
                                               "seller"
                                               (:user-id body))]
        (response/ok (:seller-team (d-teams/get-by-buyersphere db
                                                               (:id organization)
                                                               id))))
      (response/unauthorized))))
