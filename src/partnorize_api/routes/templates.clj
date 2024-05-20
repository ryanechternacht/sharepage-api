(ns partnorize-api.routes.templates
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.buyersphere-activity-templates :as d-act-templ]
            [partnorize-api.data.buyersphere-page-templates :as d-page-templ]
            [partnorize-api.data.permission :as d-permission]
            [partnorize-api.external-api.open-ai :as open-ai]
            [ring.util.http-response :as response]))

(def GET-template-milestones
  (cpj/GET "/v0.1/templates/milestones" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-act-templ/get-milestone-templates db
                                                        (:id organization)))
      (response/unauthorized))))

(def POST-template-milestones
  (cpj/POST "/v0.1/templates/milestones" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-act-templ/create-milestone-template
                    db
                    (:id organization)
                    body))
      (response/unauthorized))))

(def PATCH-template-milestone
  (cpj/PATCH "/v0.1/templates/milestone/:id"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-act-templ/update-milestone-template
                    db
                    (:id organization)
                    id
                    body))
      (response/unauthorized))))

(def DELETE-template-milestone
  (cpj/DELETE "/v0.1/templates/milestone/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-act-templ/delete-milestone-template
                    db
                    (:id organization)
                    id))
      (response/unauthorized))))

(def GET-template-activities
  (cpj/GET "/v0.1/templates/activities" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-act-templ/get-activity-templates db
                                                        (:id organization)))
      (response/unauthorized))))

(def POST-template-activities
  (cpj/POST "/v0.1/templates/milestone/:id/activities"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-act-templ/create-activity-template
                    db
                    (:id organization)
                    id
                    body))
      (response/unauthorized))))

(def PATCH-template-activity
  (cpj/PATCH "/v0.1/templates/activity/:id"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-act-templ/update-activity-template
                    db
                    (:id organization)
                    id
                    body))
      (response/unauthorized))))

(def DELETE-template-activity
  (cpj/DELETE "/v0.1/templates/activity/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-act-templ/delete-activity-template
                    db
                    (:id organization)
                    id))
      (response/unauthorized))))

(def GET-template-pages
  (cpj/GET "/v0.1/templates/pages" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-page-templ/get-buyersphere-page-templates db
                                                                (:id organization)))
      (response/unauthorized))))

(def POST-template-pages
  (cpj/POST "/v0.1/templates/pages" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-page-templ/create-buyersphere-page-template
                    db
                    (:id organization)
                    body))
      (response/unauthorized))))

(def PATCH-template-page
  (cpj/PATCH "/v0.1/templates/page/:id"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-page-templ/update-buyersphere-page-template
                    db
                    (:id organization)
                    id
                    body))
      (response/unauthorized))))

(def DELETE-template-page
  (cpj/DELETE "/v0.1/templates/page/:id" [id :<< coerce/as-int :as {:keys [db user organization]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-page-templ/delete-buyersphere-page-template
                    db
                    (:id organization)
                    id))
      (response/unauthorized))))

(def POST-templates-generate-text
  (cpj/POST "/v0.1/templates/generate-text" {:keys [db organization user config body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok {:text (open-ai/generate-message (:open-ai config) (:prompt body))})
      (response/unauthorized))))
