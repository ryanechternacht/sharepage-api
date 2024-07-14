(ns partnorize-api.routes
  (:require [compojure.core :as cpj]
            [partnorize-api.routes.activities :as activities]
            [partnorize-api.routes.auth :as auth]
            [partnorize-api.routes.buyer-activity :as buyer-activity]
            [partnorize-api.routes.buyer-sessions :as buyer-sessions]
            [partnorize-api.routes.buyerspheres :as buyerspheres]
            [partnorize-api.routes.conversation-templates :as conversation-templates]
            [partnorize-api.routes.campaigns :as campaigns]
            [partnorize-api.routes.features :as features]
            [partnorize-api.routes.organization :as organization]
            [partnorize-api.routes.pain-points :as pain-points]
            [partnorize-api.routes.pricing :as pricing]
            [partnorize-api.routes.resources :as resources]
            [partnorize-api.routes.salesforce :as salesforce]
            [partnorize-api.routes.swaypages :as swaypages]
            [partnorize-api.routes.templates :as templates]
            [partnorize-api.routes.users :as users]
            [partnorize-api.routes.virtual-swaypages :as virtual-swaypages]
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
  #'activities/GET-activities-2
  #'auth/GET-login
  #'auth/GET-auth-salesforce
  #'auth/POST-send-magic-link-login-email
  #'auth/POST-signup
  #'buyer-activity/GET-buyer-activity
  #'buyer-activity/POST-activity
  #'buyer-sessions/GET-buyer-sessions
  #'buyerspheres/GET-buyerspheres
  #'buyerspheres/GET-buyersphere
  #'buyerspheres/GET-buyersphere-by-shortcode
  #'buyerspheres/POST-buyersphere
  #'buyerspheres/PATCH-buyersphere
  #'buyerspheres/GET-buyersphere-milestones
  #'buyerspheres/POST-buyersphere-milestones
  #'buyerspheres/PATCH-buyersphere-milestone
  #'buyerspheres/DELETE-buyersphere-milestone
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
  #'buyerspheres/GET-buyersphere-pages
  #'buyerspheres/POST-buyersphere-pages
  #'buyerspheres/PATCH-buyersphere-pages-ordering
  #'buyerspheres/PATCH-buyersphere-page
  #'buyerspheres/DELETE-buyersphere-page
  #'buyerspheres/GET-buyersphere-links
  #'buyerspheres/POST-buyersphere-links
  #'buyerspheres/PATCH-buyersphere-links-ordering
  #'buyerspheres/PATCH-buyersphere-link
  #'buyerspheres/DELETE-buyersphere-link
  #'buyerspheres/POST-buyersphere-session
  #'buyerspheres/POST-buyersphere-session-timing
  #'buyerspheres/POST-buyersphere-session-event
  #'buyerspheres/GET-buyersphere-sessions
  #'buyerspheres/POST-buyerspheres-template
  #'campaigns/POST-campaigns
  #'campaigns/GET-campaign
  #'campaigns/PATCH-campaign
  #'campaigns/POST-campaign-publish
  #'campaigns/GET-campaign-published-csv
  #'campaigns/GET-campaigns
  #'campaigns/GET-campaign-swaypages
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
  #'swaypages/GET-swaypages
  #'swaypages/GET-swaypage
  #'swaypages/GET-swaypage-by-shortcode
  #'swaypages/PATCH-swaypage
  #'templates/GET-template-milestones
  #'templates/POST-template-milestones
  #'templates/PATCH-template-milestone
  #'templates/DELETE-template-milestone
  #'templates/GET-template-activities
  #'templates/POST-template-activities
  #'templates/PATCH-template-activity
  #'templates/DELETE-template-activity
  #'templates/GET-template-pages
  #'templates/POST-template-pages
  #'templates/PATCH-template-page
  #'templates/DELETE-template-page
  #'templates/POST-templates-generate-text
  #'users/GET-users
  #'users/GET-users-me
  #'users/GET-users-me-buyerspheres
  #'users/POST-users
  #'users/PATCH-users
  #'virtual-swaypages/GET-virtual-swaypage
  get-404
  post-404
  patch-404
  put-404
  delete-404)
