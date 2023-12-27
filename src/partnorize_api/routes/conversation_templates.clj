(ns partnorize-api.routes.conversation_templates
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.conversation-templates :as d-conversation-templates]
            [partnorize-api.data.permission :as d-permission]
            [partnorize-api.data.teams :as d-teams]
            [partnorize-api.data.users :as d-users]
            [ring.util.http-response :as response]))

(def GET-conversation-template
  (cpj/GET "/v0.1/conversation-template" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-conversation-templates/get-by-organization db
                                                                 (:id organization)))
      (response/unauthorized))))

(def POST-conversation-template-item
  (cpj/POST "/v0.1/conversation-template/item" {:keys [db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-conversation-templates/create-conversation-template-item
                    db 
                    (:id organization)
                    (:message body)
                    (:due-date-days body)
                    (:assigned-team body)
                    (:collaboration-type body)))
      (response/unauthorized))))

(def PATCH-conversation-template-item
  (cpj/PATCH "/v0.1/conversation-template/item/:id"
    [id :<< coerce/as-int :as {:keys [db user organization body]}]
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-conversation-templates/update-conversation-template-item
                    db
                    (:id organization)
                    id
                    body))
      (response/unauthorized))))
