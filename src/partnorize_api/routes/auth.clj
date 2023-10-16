(ns partnorize-api.routes.auth
  (:require [compojure.core :as cpj]
            [lambdaisland.uri :as uri]
            [ring.util.http-response :as response]
            [partnorize-api.data.organizations :as d-org]
            [partnorize-api.data.users :as d-users]
            [partnorize-api.data.utilities :as util]
            [partnorize-api.external-api.stytch :as stytch]))

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

(def POST-send-magic-link-login-email
  (cpj/POST "/v0.1/send-magic-link-login-email" {:keys [db organization config body subdomain]}
    (println "organization" organization)
    (println "body" body)
    (println "subdomain" subdomain)
    (let [email (:user-email body)
          _ (println "email" email)
          org (if (not= subdomain "app")
                organization
                (when-let [u (first (d-users/get-by-email-global db email))]
                  (println "user" u)
                  (util/kebab-case (d-org/get-by-id db (:organization_id u)))))
          _ (println "org" org)]
      (if (and org
               (stytch/send-magic-link-email
                (:stytch config)
                (:stytch-organization-id org)
                email))
        (response/ok "Email sent")
        (response/bad-request "Email could not be sent")))))