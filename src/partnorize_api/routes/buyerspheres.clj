(ns partnorize-api.routes.buyerspheres
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.buyer-tracking :as d-buyer-tracking]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.data.buyersphere-activities :as d-buyer-activities]
            [partnorize-api.data.buyersphere-notes :as d-buyer-notes]
            [partnorize-api.data.buyersphere-resources :as d-buyer-res]
            [partnorize-api.data.conversations :as d-conversations]
            [partnorize-api.data.buyersphere-pages :as d-pages]
            [partnorize-api.data.permission :as d-permission]
            [partnorize-api.data.users :as d-users]
            [partnorize-api.data.teams :as d-teams]
            [ring.util.http-response :as response]))

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
  (cpj/GET "/v0.1/buyersphere/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/is-buyersphere-visible? db organization id user)
      (response/ok (d-buyerspheres/get-full-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def GET-buyersphere-by-shortcode
  (cpj/GET "/v0.1/buyersphere/shortcode/:shortcode" [shortcode :as {:keys [db user organization] :as req}]
    (let [bs (d-buyerspheres/get-by-shortcode db (:id organization) shortcode)]
      (if bs
        (response/ok bs)
        (response/not-found)))))

(def POST-buyersphere
  (cpj/POST "/v0.1/buyerspheres" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-buyerspheres/create-buyersphere-coordinator db (:id organization) (:id user) body))
      (response/unauthorized))))

;; TODO technically a buyer can update anything in a buyersphere if they craft the message correctly
(def PATCH-buyersphere
  (cpj/PATCH "/v0.1/buyersphere/:id" 
    [id :<< coerce/as-int :as {:keys [db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization id user)
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
          (d-buyer-tracking/track-activity-if-buyer-coordinator
           db
           (:id organization)
           id
           (:id user)
           anonymous-user
           activity-type
           {:id id
            :buyer buyer}))
        (response/ok updated-buyersphere))
      (response/unauthorized))))

(def GET-buyersphere-milestones
  (cpj/GET "/v0.1/buyersphere/:id/milestones" [id :<< coerce/as-int :as {:keys [db user organization]}]
   (if (d-permission/is-buyersphere-visible? db organization id user)
     (response/ok (d-buyer-activities/get-milestones-for-buyersphere db (:id organization) id))
     (response/unauthorized))))

(def POST-buyersphere-milestones
  (cpj/POST "/v0.1/buyersphere/:id/milestones"
    [id :<< coerce/as-int :as {:keys [db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization id user)
      (let [{:keys [id title] :as new-activity}
            (d-buyer-activities/create-milestone db
                                                (:id organization)
                                                id
                                                body)]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         id
         (:id user)
         anonymous-user
         "create-milestone"
         {:id id
          :title title})
        (response/ok new-activity))
      (response/unauthorized))))

(def PATCH-buyersphere-milestone
  (cpj/PATCH "/v0.1/buyersphere/:b-id/milestone/:m-id"
    [b-id :<< coerce/as-int m-id :<< coerce/as-int
     :as {:keys [db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (let [{:keys [id title] :as updated-milestone}
            (d-buyer-activities/update-milestone db
                                                 (:id organization)
                                                 b-id
                                                 m-id
                                                 body)]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         b-id
         (:id user)
         anonymous-user
         "edit-milestone"
         {:id m-id
          :title title})
        (response/ok updated-milestone))
      (response/unauthorized))))

(def DELETE-buyersphere-milestone
  (cpj/DELETE "/v0.1/buyersphere/:b-id/milestone/:m-id"
    [b-id :<< coerce/as-int m-id :<< coerce/as-int :as {:keys [db user anonymous-user organization]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (let [{:keys [title] :as deleted-conversation}
            (d-buyer-activities/delete-milestone db
                                                 (:id organization)
                                                 b-id
                                                 m-id)]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         b-id
         (:id user)
         anonymous-user
         "delete-milestone"
         {:id m-id
          :title title})
        (response/ok deleted-conversation))
      (response/unauthorized))))

(def GET-buyersphere-activities
  (cpj/GET "/v0.1/buyersphere/:id/milestones/activities" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/is-buyersphere-visible? db organization id user)
      (response/ok (d-buyer-activities/get-activities-for-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def POST-buyersphere-activities
  (cpj/POST "/v0.1/buyersphere/:b-id/milestone/:m-id/activities"
    [b-id :<< coerce/as-int m-id :<< coerce/as-int :as {:keys [db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (let [{:keys [id title] :as new-activity}
            (d-buyer-activities/create-activity db
                                                (:id organization)
                                                b-id
                                                m-id
                                                (:id user)
                                                body)]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         b-id
         (:id user)
         anonymous-user
         "create-activity"
         {:id id
          :title title})
        (response/ok new-activity))
      (response/unauthorized))))

(def PATCH-buyersphere-activity
  (cpj/PATCH "/v0.1/buyersphere/:b-id/activity/:a-id"
    [b-id :<< coerce/as-int a-id :<< coerce/as-int
     :as {:keys [db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (let [{:keys [id title] :as updated-activity}
            (d-buyer-activities/update-activity-coordinator db
                                                            (:id organization)
                                                            b-id
                                                            a-id
                                                            body)
            activity-type (cond
                            (> (count (keys body)) 1) "edit-activity"
                            (:resolved body) "resolve-activity"
                            :else "unresolve-activity")]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         b-id
         (:id user)
         anonymous-user
         activity-type
         {:id a-id
          :title title})
        (response/ok updated-activity))
      (response/unauthorized))))

(def DELETE-buyersphere-activity
  (cpj/DELETE "/v0.1/buyersphere/:b-id/activity/:a-id"
    [b-id :<< coerce/as-int a-id :<< coerce/as-int :as {:keys [db user anonymous-user organization]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (let [{:keys [title] :as deleted-conversation}
            (d-buyer-activities/delete-activity db
                                                (:id organization)
                                                b-id
                                                a-id)]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         b-id
         (:id user)
         anonymous-user
         "delete-activity"
         {:id a-id
          :title title})
        (response/ok deleted-conversation))
      (response/unauthorized))))

(def GET-buyersphere-conversations
  (cpj/GET "/v0.1/buyersphere/:id/conversations" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/is-buyersphere-visible? db organization id user)
      (response/ok (d-conversations/get-by-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def POST-buyersphere-conversations
  (cpj/POST "/v0.1/buyersphere/:id/conversations" 
    [id :<< coerce/as-int :as {:keys [db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization id user)
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
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         id
         (:id user)
         anonymous-user
         "create-activity"
         {:id id
          :message message})
        (response/ok new-conversation))
      (response/unauthorized))))

(def PATCH-buyersphere-conversation
  (cpj/PATCH "/v0.1/buyersphere/:b-id/conversation/:c-id"
    [b-id :<< coerce/as-int c-id :<< coerce/as-int :as {:keys [db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (let [{:keys [message] :as updated-conversation}
            (d-conversations/update-conversation db
                                                 (:id organization)
                                                 b-id
                                                 c-id
                                                 body)
            activity-type (cond
                            (> (count (keys body)) 1) "edit-activity"
                            (:resolved body) "resolve-activity"
                            :else "unresolve-activity")]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         b-id
         (:id user)
         anonymous-user
         activity-type
         {:id c-id
          :message message})
        (response/ok updated-conversation))
      (response/unauthorized))))

(def DELETE-buyersphere-conversation
  (cpj/DELETE "/v0.1/buyersphere/:b-id/conversation/:c-id"
    [b-id :<< coerce/as-int c-id :<< coerce/as-int :as {:keys [db user anonymous-user organization]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (let [{:keys [message] :as deleted-conversation}
            (d-conversations/delete-conversation db
                                                 (:id organization)
                                                 b-id
                                                 c-id)]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         b-id
         (:id user)
         anonymous-user
         "delete-activity"
         {:id c-id
          :message message})
        (response/ok deleted-conversation))
      (response/unauthorized))))

(def POST-buyersphere-resource
  (cpj/POST "/v0.1/buyersphere/:b-id/resources"
    [b-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (response/ok (d-buyer-res/create-buyersphere-resource db
                                                            (:id organization)
                                                            b-id
                                                            body))
      (response/unauthorized))))

(def PATCH-buyersphere-resource
  (cpj/PATCH "/v0.1/buyersphere/:b-id/resource/:r-id"
    [b-id :<< coerce/as-int r-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (response/ok (d-buyer-res/update-buyersphere-resource db
                                                            (:id organization)
                                                            b-id
                                                            r-id
                                                            body))
      (response/unauthorized))))

(def DELETE-buyersphere-resource
  (cpj/DELETE "/v0.1/buyersphere/:b-id/resource/:r-id"
    [b-id :<< coerce/as-int r-id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (response/ok (d-buyer-res/delete-buyersphere-resource db
                                                            (:id organization)
                                                            b-id
                                                            r-id))
      (response/unauthorized))))

(def POST-buyersphere-note
  (cpj/POST "/v0.1/buyersphere/:b-id/notes"
    [b-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (response/ok (d-buyer-notes/create-buyersphere-note db
                                                          (:id organization)
                                                          b-id
                                                          body))
      (response/unauthorized))))

(def PATCH-buyersphere-note
  (cpj/PATCH "/v0.1/buyersphere/:b-id/note/:n-id"
    [b-id :<< coerce/as-int n-id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (response/ok (d-buyer-notes/update-buyersphere-note db
                                                          (:id organization)
                                                          b-id
                                                          n-id
                                                          body))
      (response/unauthorized))))

(def DELETE-buyersphere-note
  (cpj/DELETE "/v0.1/buyersphere/:b-id/note/:n-id"
    [b-id :<< coerce/as-int n-id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (response/ok (d-buyer-notes/delete-buyersphere-note db
                                                          (:id organization)
                                                          b-id
                                                          n-id))
      (response/unauthorized))))

(def POST-add-buyer-to-buyersphere
  (cpj/POST "/v0.1/buyersphere/:id/team/buyer"
    [id :<< coerce/as-int :as {:keys [config db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization id user)
      (let [new-user (d-users/create-user config
                                          db
                                          organization
                                          "buyer"
                                          body)]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         id
         (:id user)
         anonymous-user
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
  (cpj/PATCH "/v0.1/buyersphere/:b-id/team/buyer/:u-id"
    [b-id :<< coerce/as-int u-id :<< coerce/as-int
     :as {:keys [db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (do
        (d-teams/edit-user-in-buyersphere db
                                          (:id organization)
                                          u-id
                                          body)
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         b-id
         (:id user)
         anonymous-user
         "edit-user"
         {:user (:email body)})
        (response/ok (:buyer-team (d-teams/get-by-buyersphere db
                                                              (:id organization)
                                                              b-id))))
      (response/unauthorized))))

(def DELETE-remove-buyer-from-buyersphere
  (cpj/DELETE "/v0.1/buyersphere/:b-id/team/buyer/:u-id"
    [b-id :<< coerce/as-int u-id :<< coerce/as-int
     :as {:keys [db user anonymous-user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (let [deleted-user
            (d-teams/remove-user-from-buyersphere-coordinator db
                                                              (:id organization)
                                                              b-id
                                                              u-id)]
        (d-buyer-tracking/track-activity-if-buyer-coordinator
         db
         (:id organization)
         b-id
         (:id user)
         anonymous-user
         "remove-user"
         {:user (:email deleted-user)})
        (response/ok (:buyer-team (d-teams/get-by-buyersphere db
                                                              (:id organization)
                                                              b-id))))
      (response/unauthorized))))

(def POST-add-seller-to-buyersphere
  (cpj/POST "/v0.1/buyersphere/:id/team/seller"
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
  (cpj/GET "/v0.1/buyersphere/:id/buyer-activity" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-edit-buyersphere? db organization id user)
      (response/ok (d-buyer-tracking/get-tracking-for-buyersphere db (:id organization) id))
      (response/unauthorized))))

(def GET-buyersphere-pages
  (cpj/GET "/v0.1/buyerspheres/:id/pages" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/is-buyersphere-visible? db organization id user)
      (response/ok (d-pages/get-buyersphere-pages db (:id organization) id))
      (response/unauthorized))))

(def POST-buyersphere-pages
  (cpj/POST "/v0.1/buyerspheres/:id/pages"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/can-user-edit-buyersphere? db organization id user)
      (response/ok (d-pages/create-buyersphere-page-coordinator db
                                                                (:id organization)
                                                                id
                                                                body))
      (response/unauthorized))))

(def PATCH-buyersphere-page
  (cpj/PATCH "/v0.1/buyerspheres/:b-id/page/:p-id"
    [b-id :<< coerce/as-int p-id :<< coerce/as-int :as {:keys [db user anonymous-user organization body]}]
    (let [{:keys [can_buyer_edit]} (d-pages/get-buyersphere-page db
                                                                 (:id organization)
                                                                 b-id
                                                                 p-id)]
      (if (or can_buyer_edit
              (d-permission/can-user-edit-buyersphere? db organization b-id user))
        (let [{new-body :body new-title :title :as updated-page}
              (d-pages/update-buyersphere-page db
                                               (:id organization)
                                               b-id
                                               p-id
                                               body)]
          (d-buyer-tracking/track-activity-if-buyer-coordinator
           db
           (:id organization)
           b-id
           (:id user)
           anonymous-user
           "edit-page"
           {:buyersphere-id b-id
            :id p-id
            :body new-body
            :title new-title})
          (response/ok updated-page))
        (response/unauthorized)))))

(def DELETE-buyersphere-page
  (cpj/DELETE "/v0.1/buyerspheres/:b-id/page/:p-id"
    [b-id :<< coerce/as-int p-id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/can-user-edit-buyersphere? db organization b-id user)
      (response/ok (d-pages/delete-buyersphere-page db
                                                    (:id organization)
                                                    b-id
                                                    p-id))
      (response/unauthorized))))
