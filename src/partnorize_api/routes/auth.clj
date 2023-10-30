(ns partnorize-api.routes.auth
  (:require [compojure.core :as cpj]
            [lambdaisland.uri :as uri]
            [partnorize-api.data.organizations :as d-org]
            [partnorize-api.data.users :as d-users]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.external-api.stytch :as stytch]
            [ring.util.http-response :as response]))

(defn set-session [session_token response]
  ;; TODO only on local
  ;; this is needed because we can only set use http for localhost in stytch, and I haven't setup
  ;; https locally yet
  (println "for browser:" "buyersphere-session" session_token ".buyersphere-local.com")
  (println "for postman" (format "buyersphere-session=%s" session_token))
  (assoc response :session session_token))

(defn- make-url [base-url subdomain path]
  (-> (uri/uri base-url)
      (update :host #(str subdomain "." %))
      (assoc :path path)
      str))

(defn- magic-link-login [db stytch-config front-end-base-url slug token]
  (let [org (d-org/get-by-subdomain db slug)
        {session-token :session_token
         {:keys [email_address name oauth_registrations]} :member} (stytch/authenticate-magic-link stytch-config token)]
    (if (and org session-token)
      (do
        (d-users/update-user-from-stytch db email_address name (-> oauth_registrations first :profile_picture_url))
        (set-session session-token (response/found (make-url front-end-base-url slug ""))))
      (response/found (make-url front-end-base-url slug "/login")))))

(defn- oauth-login [db stytch-config front-end-base-url slug token]
  (let [org (d-org/get-by-subdomain db slug)
        {session-token :session_token
         {:keys [email_address name oauth_registrations]} :member} (stytch/authenticate-oauth stytch-config token)]
    (if (and org session-token)
      (do
        (d-users/update-user-from-stytch db email_address name (-> oauth_registrations first :profile_picture_url))
        (set-session session-token (response/found (make-url front-end-base-url slug ""))))
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

;; (-> b)

;; 00DHs000002k3xp!AQcAQPsEifb49EI1pp2WODpi1DqIg9fcrzGgqSQaQxur4MX6_3A7M42qQLYZrM_BXwuHKhpGJl43NayVWVPLUzstNPsxsk2A

(def GET-auth-salesforce
  (cpj/GET "/v0.1/auth/salesforce" [code :as body]
    (println code)
    (def b body)
    (response/ok)))

(def POST-send-magic-link-login-email
  (cpj/POST "/v0.1/send-magic-link-login-email" {:keys [db organization config body subdomain]}
    (let [email (:user-email body)
          org (if (not= subdomain "app")
                organization
                (when-let [u (first (d-users/get-by-email-global db email))]
                  (u/kebab-case (d-org/get-by-id db (:organization_id u)))))]
      (if (and org
               (stytch/send-magic-link-email
                (:stytch config)
                (:stytch-organization-id org)
                email))
        (response/ok "Email sent")
        (response/bad-request "Email could not be sent")))))
