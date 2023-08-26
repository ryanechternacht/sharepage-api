(ns partnorize-api.routes.users
  (:require [compojure.core :as cpj]
            [partnorize-api.data.users :as d-users]
            [ring.util.http-response :as response]))

(def GET-users-me
  (cpj/GET "/v0.1/users/me" {user :user}
    (if user
      (response/ok user)
      (response/unauthorized))))

(def GET-users
  (cpj/GET "/v0.1/users" {:keys [db user organization]}
    (if user
      (response/ok (d-users/get-by-organization db
                                                (:id organization)))
      (response/unauthorized))))

(def POST-users
  (cpj/POST "/v0.1/users" {:keys [db user organization body]}
    (if user
      ;; TODO add the rest of the create user stytch code
      (response/ok (d-users/create-user db
                                        (:id organization)
                                        body))
      (response/unauthorized))))
