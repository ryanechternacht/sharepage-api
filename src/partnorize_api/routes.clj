(ns partnorize-api.routes
  (:require [compojure.core :as cpj]
            [partnorize-api.routes.activities :as activities]
            [partnorize-api.routes.auth :as auth]
            [partnorize-api.routes.buyer-activity :as buyer-activity]
            [partnorize-api.routes.buyerspheres :as buyerspheres]
            [partnorize-api.routes.conversation-templates :as conversation-templates]
            ;; [partnorize-api.routes.deal-timing :as deal-timing]
            [partnorize-api.routes.features :as features]
            [partnorize-api.routes.organization :as organization]
            [partnorize-api.routes.pain-points :as pain-points]
            ;; [partnorize-api.routes.personas :as personas]
            [partnorize-api.routes.pricing :as pricing]
            [partnorize-api.routes.resources :as resources]
            [partnorize-api.routes.salesforce :as salesforce]
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
  #'activities/GET-activities
  #'auth/GET-login
  #'auth/GET-auth-salesforce
  #'auth/POST-send-magic-link-login-email
  #'buyer-activity/GET-buyer-activity
  #'buyer-activity/POST-activity
  #'buyerspheres/GET-buyerspheres
  #'buyerspheres/GET-buyersphere
  #'buyerspheres/POST-buyersphere
  #'buyerspheres/PATCH-buyersphere
  #'buyerspheres/GET-buyersphere-milestones
  #'buyerspheres/POST-buyersphere-milestones
  #'buyerspheres/PATCH-buyersphere-milestone
  #'buyerspheres/GET-buyersphere-activities
  #'buyerspheres/POST-buyersphere-activities
  #'buyerspheres/PATCH-buyersphere-activity
  #'buyerspheres/DELETE-buyersphere-activity
  #'buyerspheres/GET-buyersphere-conversations
  #'buyerspheres/POST-buyersphere-conversations
  #'buyerspheres/PATCH-buyersphere-conversation
  #'buyerspheres/DELETE-buyersphere-conversation
  #'buyerspheres/POST-buyersphere-note
  #'buyerspheres/PATCH-buyersphere-note
  #'buyerspheres/DELETE-buyersphere-note
  #'buyerspheres/POST-buyersphere-resource
  #'buyerspheres/PATCH-buyersphere-resource
  #'buyerspheres/DELETE-buyersphere-resource
  #'buyerspheres/POST-add-buyer-to-buyersphere
  #'buyerspheres/PATCH-edit-buyer-in-buyersphere
  #'buyerspheres/DELETE-remove-buyer-from-buyersphere
  #'buyerspheres/POST-add-seller-to-buyersphere
  #'buyerspheres/GET-buyersphere-buyer-activity
  #'conversation-templates/GET-conversation-template
  #'conversation-templates/POST-conversation-template-item
  #'conversation-templates/PATCH-conversation-template-item
  #'conversation-templates/DELETE-conversation-template-item
  ;; #'deal-timing/GET-deal-timing
  ;; #'deal-timing/PUT-deal-timing
  #'features/GET-features
  #'features/GET-features-for-buyersphere
  #'features/POST-features
  #'features/PUT-features
  #'features/DELETE-features
  #'organization/GET-organization
  #'organization/PATCH-organization
  #'pain-points/GET-pain-points
  #'pain-points/GET-pain-points-for-buyersphere
  #'pain-points/POST-pain-points
  #'pain-points/PUT-pain-points
  #'pain-points/DELETE-pain-points
  ;; #'personas/GET-personas
  ;; #'personas/POST-personas
  ;; #'personas/PUT-personas
  ;; #'personas/DELETE-personas
  #'pricing/GET-pricing
  #'pricing/GET-pricing-for-buyersphere
  #'pricing/PUT-pricing
  #'pricing/POST-pricing-tiers
  #'pricing/PUT-pricing-tiers
  #'pricing/DELETE-pricing-tiers
  #'resources/GET-resources
  #'resources/POST-resources
  #'resources/PUT-resources
  #'resources/DELETE-resources
  #'salesforce/GET-opportunities
  #'users/GET-users
  #'users/GET-users-me
  #'users/GET-users-me-buyerspheres
  #'users/POST-users
  #'users/PATCH-users
  get-404
  post-404
  patch-404
  put-404
  delete-404)
