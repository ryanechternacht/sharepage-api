(ns partnorize-api.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [partnorize-api.routes.auth :as auth]
            [partnorize-api.routes.buyerspheres :as buyerspheres]
            [partnorize-api.routes.features :as features]
            [partnorize-api.routes.pain-points :as pain-points]
            [partnorize-api.routes.personas :as personas]
            [ring.util.response :refer [response not-found]]))

(def GET-root-healthz
  (GET "/" []
    (response "I'm here")))

(def get-404
  (GET "*" []
    (not-found nil)))

(def post-404
  (POST "*" []
    (not-found nil)))

(defroutes routes
  #'GET-root-healthz
  #'auth/GET-login
  #'auth/POST-send-magic-link-login-email
  #'buyerspheres/GET-buyerspheres
  #'buyerspheres/PATCH-buyerspheres-features
  #'features/GET-features
  #'pain-points/GET-pain-points
  #'personas/GET-personas
  get-404
  post-404)
