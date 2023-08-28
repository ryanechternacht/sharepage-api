(ns partnorize-api.routes
  (:require [compojure.core :as cpj]
            [partnorize-api.routes.auth :as auth]
            [partnorize-api.routes.buyerspheres :as buyerspheres]
            [partnorize-api.routes.deal-timing :as deal-timing]
            [partnorize-api.routes.features :as features]
            [partnorize-api.routes.organization :as organization]
            [partnorize-api.routes.pain-points :as pain-points]
            [partnorize-api.routes.personas :as personas]
            [partnorize-api.routes.pricing-tiers :as pricing-tiers]
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
  #'buyerspheres/GET-buyersphere
  #'buyerspheres/PATCH-buyersphere
  #'buyerspheres/GET-buyersphere-conversations
  #'buyerspheres/POST-buyersphere-conversations
  #'deal-timing/GET-deal-timing
  #'deal-timing/PUT-deal-timing
  #'features/GET-features
  #'features/POST-features
  #'features/PUT-features
  #'features/DELETE-features
  #'organization/GET-organization
  #'pain-points/GET-pain-points
  #'pain-points/POST-pain-points
  #'pain-points/PUT-pain-points
  #'pain-points/DELETE-pain-points
  #'personas/GET-personas
  #'personas/POST-personas
  #'personas/PUT-personas
  #'personas/DELETE-personas
  #'pricing-tiers/GET-pricing-tiers
  #'pricing-tiers/POST-pricing-tiers
  #'pricing-tiers/PUT-pricing-tiers
  #'pricing-tiers/DELETE-pricing-tiers
  #'users/GET-users
  #'users/GET-users-me
  #'users/POST-users
  get-404
  post-404
  patch-404
  put-404
  delete-404)
