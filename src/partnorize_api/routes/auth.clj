(ns partnorize-api.routes.auth
  (:require [compojure.core :as cpj]
            [lambdaisland.uri :as uri]
            [partnorize-api.data.organizations :as d-org]
            [partnorize-api.data.users :as d-users]
            [partnorize-api.data.utilities :as u]
            [partnorize-api.data.salesforce-access :as d-sf]
            [partnorize-api.data.teams :as d-teams]
            [partnorize-api.external-api.salesforce :as sf]
            [partnorize-api.external-api.stytch :as stytch]
            [ring.util.http-response :as response]))

(defn set-session [session_token response]
  ;; TODO only on local
  ;; this is needed because we can only set use http for localhost in stytch, and I haven't setup
  ;; https locally yet
  (println "for browser:" "buyersphere-session" session_token ".buyersphere-local.com")
  (println "for postman:" (format "buyersphere-session=%s" session_token))
  (assoc response :session session_token))

(defn- make-url [base-url subdomain path]
  (-> (uri/uri base-url)
      (update :host #(str subdomain "." %))
      (assoc :path path)
      str))

;; TODO should we save the session ids from magic-link-login and 
;; oauth-login so that we don't need to reauth them right away?
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

(def GET-auth-salesforce
  (cpj/GET "/v0.1/auth/salesforce" [code state :as {:keys [db organization user config]}]
    (if code
      ;; returning from SF
      (let [{:keys [access_token instance_url refresh_token id]} (sf/get-sf-access-token (:salesforce config) code)
            {sf-user-id :user_id} (sf/get-sf-user-info access_token id)
            {:keys [organization-id user-id]} (u/base-64-decode-clj state)
            {subdomain :subdomain} (d-org/get-by-id db organization-id)
            bs-landing-page (make-url (-> config :front-end :base-url) subdomain "/salesforce")]
        (d-sf/save-salesforce-access-details! db organization-id user-id access_token instance_url refresh_token sf-user-id)
        (response/found bs-landing-page))
      ;; send to SF
      (let [state (u/base-64-encode-clj {:organization-id (:id organization)
                                         :user-id (:id user)})]
        (response/found (sf/generate-salesforce-login-link (:salesforce config) state))))))

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

(def POST-signup
  (cpj/POST "/v0.1/signup" {:keys [db organization config body subdomain]}
    (let [email (:user-email body)
          swaypage-id (parse-long (:swaypage-id body))
          user (d-users/get-by-email db (:id organization) email)]
      (cond
        user (do
               (stytch/send-magic-link-email
                (:stytch config)
                (:stytch-organization-id organization)
                email)
               (response/bad-request "User already exists. Login email sent."))
        (= subdomain "app") (response/bad-request "Can only register on a real org")

        :else
        (do
          (let [new-user (d-users/create-user
                          config
                          db
                          organization
                          "buyer"
                          body)]
            (when swaypage-id
              (d-teams/add-user-to-buyersphere db
                                               (:id organization)
                                               swaypage-id
                                               "buyer"
                                               (:id new-user)))
            (response/ok "Signup successful. Check email for login link.")))))))
