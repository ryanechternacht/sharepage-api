(ns partnorize-api.routes
  (:require [compojure.core :as cpj]
            [partnorize-api.routes.auth :as auth]
            [partnorize-api.routes.buyerspheres :as buyerspheres]
            [partnorize-api.routes.features :as features]
            [partnorize-api.routes.organization :as organization]
            [partnorize-api.routes.pain-points :as pain-points]
            [partnorize-api.routes.personas :as personas]
            [partnorize-api.routes.users :as users]
            [ring.util.http-response :as response]))

(def GET-root-healthz
  (cpj/GET "/" []
    (response/ok "I'm here")))

(def get-404
  (cpj/GET "*" []
    (response/not-found)))

(def post-404
  (cpj/POST "*" []
    (response/not-found)))

(def patch-404
  (cpj/PATCH "*" []
    (response/not-found)))

(def put-404
  (cpj/PUT "*" []
    (response/not-found)))

(def delete-404
  (cpj/DELETE "*" []
    (response/not-found)))

(cpj/defroutes routes
  #'GET-root-healthz
  #'auth/GET-login
  #'auth/POST-send-magic-link-login-email
  #'buyerspheres/GET-buyerspheres
  #'buyerspheres/PATCH-buyerspheres-features
  #'buyerspheres/GET-buyerspheres-conversations
  #'buyerspheres/POST-buyerspheres-conversations
  #'features/GET-features
  #'features/POST-feature
  #'organization/GET-organization
  #'pain-points/GET-pain-points
  #'pain-points/POST-pain-points
  #'personas/GET-personas
  #'personas/POST-personas
  #'users/GET-users
  #'users/GET-users-me
  #'users/POST-users
  get-404
  post-404
  patch-404
  put-404
  delete-404)
