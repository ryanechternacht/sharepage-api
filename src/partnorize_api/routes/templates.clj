(ns partnorize-api.routes.templates
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.buyersphere-activity-templates :as d-templates]
            [partnorize-api.data.permission :as d-permission]
            [ring.util.http-response :as response]))

(def GET-template-milestones
  (cpj/GET "/v0.1/templates/milestones" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-templates/get-milestone-templates db
                                                        (:id organization)))
      (response/unauthorized))))

(def POST-template-milestones
  (cpj/POST "/v0.1/templates/milestones" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-templates/create-milestone-template
                    db
                    (:id organization)
                    body))
      (response/unauthorized))))

(def PATCH-template-milestone
  (cpj/PATCH "/v0.1/templates/milestone/:id"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-templates/update-milestone-template
                    db
                    (:id organization)
                    id
                    body))
      (response/unauthorized))))

(def DELETE-template-milestone
  (cpj/DELETE "/v0.1/templates/milestone/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-templates/delete-milestone-template
                    db
                    (:id organization)
                    id))
      (response/unauthorized))))

(def GET-template-activities
  (cpj/GET "/v0.1/templates/activities" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-templates/get-activity-templates db
                                                        (:id organization)))
      (response/unauthorized))))

(def POST-template-activities
  (cpj/POST "/v0.1/templates/milestone/:id/activities"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-templates/create-activity-template
                    db
                    (:id organization)
                    id
                    body))
      (response/unauthorized))))

(def PATCH-template-activity
  (cpj/PATCH "/v0.1/templates/activity/:id"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-templates/update-activity-template
                    db
                    (:id organization)
                    id
                    body))
      (response/unauthorized))))

(def DELETE-template-activity
  (cpj/DELETE "/v0.1/templates/activity/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-templates/delete-activity-template
                    db
                    (:id organization)
                    id))
      (response/unauthorized))))
