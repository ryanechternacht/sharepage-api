(ns partnorize-api.routes.users
  (:require [compojure.core :refer [GET]]
            [ring.util.http-response :as response]))

(def GET-users-me
  (GET "/v0.1/users/me" {user :user}
    (if user
      (response/ok user)
      (response/unauthorized))))

;; (def GET-auth0-callback
;;   (GET "/v0.1/auth0/callback/:service"
;;     [code service :as {{auth0-config :auth0 front-end-config :front-end} :config db :db}]
;;     (let [auth0-user (auth0/get-auth0-user auth0-config service code)
;;           user (d-users/get-or-create-user db auth0-user)]
;;       (set-session (redirect (:base-url front-end-config)) (:id user)))))

;; (def GET-login
;;   (GET "/v0.1/login/:service" [service :as {{auth0-config :auth0} :config}]
;;     (redirect (auth0/get-auth0-login-page auth0-config service))))
