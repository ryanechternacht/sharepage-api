(ns partnorize-api.routes.users
  (:require [compojure.core :refer [GET]]
            [ring.util.response :refer [response redirect]]))
            ;; [yardstick-api.apis.auth0 :as auth0]
            ;; [yardstick-api.data.users :as d-users]))

;; (def fail-401
;;   {:status 401
;;    :headers {}})

;; (defn set-session [response user-id]
;;   (assoc response :session (vary-meta {:user-id user-id}
;;                                       assoc :recreate true)))

;; (def GET-me
;;   (GET "/v0.1/users/me" [:as {db :db {user-id :user-id} :session}]
;;     (let [user (d-users/get-user-by-id db user-id)]
;;       (if user
;;         (response user)
;;         fail-401))))

;; (def GET-auth0-callback
;;   (GET "/v0.1/auth0/callback/:service"
;;     [code service :as {{auth0-config :auth0 front-end-config :front-end} :config db :db}]
;;     (let [auth0-user (auth0/get-auth0-user auth0-config service code)
;;           user (d-users/get-or-create-user db auth0-user)]
;;       (set-session (redirect (:base-url front-end-config)) (:id user)))))

;; (def GET-login
;;   (GET "/v0.1/login/:service" [service :as {{auth0-config :auth0} :config}]
;;     (redirect (auth0/get-auth0-login-page auth0-config service))))
