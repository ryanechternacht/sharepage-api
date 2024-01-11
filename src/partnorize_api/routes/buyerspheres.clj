(ns partnorize-api.routes.buyerspheres
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.buyer-tracking :as d-buyer-tracking]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.data.buyersphere-notes :as d-buyer-notes]
            [partnorize-api.data.buyersphere-resources :as d-buyer-res]
            [partnorize-api.data.conversations :as d-conversations]
            [partnorize-api.data.permission :as d-permission]
            [partnorize-api.data.users :as d-users]
            [partnorize-api.data.teams :as d-teams]
            [ring.util.http-response :as response]))

;; TODO find a way to automate org-id and user checks
(def GET-buyerspheres
  (cpj/GET "/v0.1/buyerspheres" [user-id stage status :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-buyerspheres/get-by-organization db
                                                       (:id organization)
                                                       {:user-id (coerce/as-int user-id)
                                                        :stage stage
                                                        :status status}))
      (response/unauthorized))))

(def GET-buyersphere
  (cpj/GET "/v0.1/buyerspheres/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (response/ok (d-buyerspheres/get-full-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def POST-buyersphere
  (cpj/POST "/v0.1/buyerspheres" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-buyerspheres/create-buyersphere-coordinator db (:id organization) (:id user) body))
      (response/unauthorized))))

;; TODO technically a buyer can update anything in a buyersphere if they craft the message correctly
(def PATCH-buyersphere
  (cpj/PATCH "/v0.1/buyerspheres/:id" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (let [{:keys [buyer] :as updated-buyersphere}
            (d-buyerspheres/update-buyersphere db
                                               (:id organization)
                                               id
                                               body)]
        (when-let [activity-type (cond
                                   (:features-answer body) "edit-features"
                                   (:constraints-answer body) "edit-constraints"
                                   (:objectives-answer body) "edit-objectives"
                                   (:success-criteria-answer body) "edit-success-criteria")]
          (d-buyer-tracking/if-user-is-buyer-track-activity-coordinator
           db
           (:id user)
           activity-type
           {:id id
            :buyer buyer}))
        (response/ok updated-buyersphere))
      (response/unauthorized))))

(def GET-buyersphere-conversations
  (cpj/GET "/v0.1/buyerspheres/:id/conversations" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (response/ok (d-conversations/get-by-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def POST-buyersphere-conversations
  (cpj/POST "/v0.1/buyerspheres/:id/conversations" [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (let [{:keys [id message] :as new-conversation}
            (d-conversations/create-conversation db
                                                 (:id organization)
                                                 id
                                                 (:id user)
                                                 (:message body)
                                                 (:due-date body)
                                                 (:assigned-to body)
                                                 (:assigned-team body)
                                                 (:collaboration-type body))]
        (d-buyer-tracking/if-user-is-buyer-track-activity-coordinator
         db
         (:id user)
         "create-activity"
         {:id id
          :message message})
        (response/ok new-conversation))
      (response/unauthorized))))

(def PATCH-buyersphere-conversation
  (cpj/PATCH "/v0.1/buyerspheres/:b-id/conversations/:c-id"
    [b-id :<< coerce/as-int c-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (let [{:keys [message] :as updated-conversation}
            (d-conversations/update-conversation db
                                                 (:id organization)
                                                 b-id
                                                 c-id
                                                 body)
            activity-type (cond
                            (not (contains? body :resolved)) "edit-activity"
                            (:resolved body) "resolve-activity"
                            :else "unresolve-activity")]
        (d-buyer-tracking/if-user-is-buyer-track-activity-coordinator
         db
         (:id user)
         activity-type
         {:id c-id
          :message message})
        (response/ok updated-conversation))
      (response/unauthorized))))

(def DELETE-buyersphere-conversation
  (cpj/DELETE "/v0.1/buyerspheres/:b-id/conversations/:c-id"
    [b-id :<< coerce/as-int c-id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (let [{:keys [message] :as deleted-conversation}
            (d-conversations/delete-conversation db
                                                 (:id organization)
                                                 b-id
                                                 c-id)]
        (d-buyer-tracking/if-user-is-buyer-track-activity-coordinator
         db
         (:id user)
         "delete-activity"
         {:id c-id
          :message message})
        (response/ok deleted-conversation))
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
                                          "buyer"
                                          body)]
        (d-buyer-tracking/if-user-is-buyer-track-activity-coordinator
         db
         (:id user)
         "invite-user"
         {:user (:email new-user)})
        (d-teams/add-user-to-buyersphere db
                                         (:id organization)
                                         id
                                         "buyer"
                                         (:id new-user))
        (response/ok (:buyer-team (d-teams/get-by-buyersphere db
                                                              (:id organization)
                                                              id))))
      (response/unauthorized))))

(def PATCH-edit-buyer-in-buyersphere
  (cpj/PATCH "/v0.1/buyerspheres/:b-id/teams/buyer/:u-id"
    [b-id :<< coerce/as-int u-id :<< coerce/as-int
     :as {:keys [db user organization body]}]
    (if (d-permission/can-user-see-buyersphere db organization b-id user)
      (let [edited-user (d-teams/edit-user-in-buyersphere db
                                                          (:id organization)
                                                          u-id
                                                          body)]
        (d-buyer-tracking/if-user-is-buyer-track-activity-coordinator
         db
         (:id user)
         "edit-user"
         {:user (:email edited-user)})

        (response/ok (:buyer-team (d-teams/get-by-buyersphere db
                                                              (:id organization)
                                                              b-id))))
      (response/unauthorized))))

(def POST-add-seller-to-buyersphere
  (cpj/POST "/v0.1/buyerspheres/:id/teams/seller"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (do
        (d-teams/add-user-to-buyersphere db
                                         (:id organization)
                                         id
                                         "seller"
                                         (:user-id body))
        (response/ok (:seller-team (d-teams/get-by-buyersphere db
                                                               (:id organization)
                                                               id))))
      (response/unauthorized))))

(def GET-buyersphere-buyer-activity
  (cpj/GET "/v0.1/buyerspheres/:id/buyer-activity" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-see-buyersphere db organization id user)
      (response/ok (d-buyer-tracking/get-tracking-for-buyersphere db (:id organization) id))
      (response/unauthorized))))
