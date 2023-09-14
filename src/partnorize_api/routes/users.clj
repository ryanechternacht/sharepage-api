(ns partnorize-api.routes.users
  (:require [compojure.core :as cpj]
            [partnorize-api.data.users :as d-users]
            [partnorize-api.data.permission :as d-permission]
            [ring.util.http-response :as response]))

(def GET-users-me
  (cpj/GET "/v0.1/users/me" {user :user}
    (if user
      (response/ok user)
      (response/unauthorized))))

(def GET-users
  (cpj/GET "/v0.1/users" {:keys [db user organization]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-users/get-by-organization db
                                                (:id organization)))
      (response/unauthorized))))

(def POST-users
  (cpj/POST "/v0.1/users" {:keys [config db user organization body]}
    (if (d-permission/does-user-have-org-permissions? db organization user)
      (response/ok (d-users/create-user config
                                        db
                                        organization
                                        body))
      (response/unauthorized))))
