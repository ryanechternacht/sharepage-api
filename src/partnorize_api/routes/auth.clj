(ns partnorize-api.routes.auth
    (:require [compojure.core :as cpj]
              [lambdaisland.uri :as uri]
              [ring.util.http-response :as response]
              [partnorize-api.data.organizations :as d-org]
              [partnorize-api.external-api.stytch :as stytch]))

(defn set-session [session_token response]
  ;; TODO only on local
  ;; this is needed because we can only set use http for localhost in stytch, and I haven't setup
  ;; https locally yet
  (println "for browser:" "ring-session" session_token ".buyersphere-local.com")
  (println "for postman" (format "ring-session=%s" session_token))
  (assoc response :session session_token))

(defn- make-url [base-url subdomain path]
  (-> (uri/uri base-url)
      (update :host #(str subdomain "." %))
      (assoc :path path)
      str))

(defn- magic-link-login [db stytch-config front-end-base-url slug token]
  (let [org (d-org/get-by-subdomain db slug)
        session-token (stytch/authenticate-magic-link stytch-config token)]
    (if (and org session-token)
      (set-session session-token (response/found (make-url front-end-base-url slug "")))
      (response/found (make-url front-end-base-url slug "/login")))))

(defn- oauth-login [db stytch-config front-end-base-url slug token]
  (let [org (d-org/get-by-subdomain db slug)
        session-token (stytch/authenticate-oauth stytch-config token)]
    (if (and org session-token)
      (set-session session-token (response/found (make-url front-end-base-url slug "")))
      (response/found (make-url front-end-base-url slug "/login")))))

(def GET-login
  (cpj/GET "/v0.1/login" [slug stytch_token_type token :as {:keys [db config]}]
    (condp = stytch_token_type
      "multi_tenant_magic_links" (magic-link-login
                                  db
                                  (:stytch config)
                                  (-> config :front-end :base-url)
                                  slug
                                  token)
      "oauth" (oauth-login
               db
               (:stytch config)
               (-> config :front-end :base-url)
               slug
               token)
      (response/bad-request "Unknown stytch_token_type"))))

;; TODO add some handling if they come from app.api... to lookup
;; the right org for them (this is how you login from the main page)
(def POST-send-magic-link-login-email
  (cpj/POST "/v0.1/send-magic-link-login-email" {:keys [organization config body]}
    (if (stytch/send-magic-link-email 
         (:stytch config) 
         (:user_email body) 
         (:stytch_organization_id organization))
      (response/ok "Email sent")
      (response/bad-request "Email could not be sent"))))
