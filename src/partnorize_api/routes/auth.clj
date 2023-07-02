(ns partnorize-api.routes.auth
    (:require [compojure.core :refer [GET POST]]
              [lambdaisland.uri :as uri]
              [ring.util.response :refer [response redirect bad-request]]
              [partnorize-api.data.organizations :as d-org]
              [partnorize-api.external-api.stytch :as stytch]))

(defn set-session [session_token response]
  (assoc response :session session_token))

(defn- make-frontend-url [frontend-base-url slug path]
  (-> (uri/uri frontend-base-url)
      (update :host #(str slug "." %))
      (assoc :path path)
      str))

(defn- magic-link-login [db stytch-config frontend-base-url slug token]
  (let [org (d-org/get-by-subdomain db slug)
        session_token (stytch/authenticate-magic-link stytch-config token)]
    (if (and org session_token)
      ;; TODO this should redirect to the right subdomain
      (set-session session_token (redirect (make-frontend-url frontend-base-url slug "")))
      (redirect (make-frontend-url frontend-base-url slug "/login")))))

(def GET-login
  (GET "/v0.1/login" [slug stytch_token_type token :as {:keys [db config]}]
    (condp = stytch_token_type
      "multi_tenant_magic_links" (magic-link-login
                                  db
                                  (:stytch config)
                                  (-> config :front-end :base-url)
                                  slug
                                  token)
      (bad-request "Unknown stytch_token_type"))))

(def POST-send-magic-link-login-email
  (POST "/v0.1/send-magic-link-login-email" {:keys [organization config body]}
    (if (stytch/send-magic-link-email (:stytch config) (:user_email body) (:stytch_organization_id organization))
      (response "Email sent")
      (bad-request "Email could not be sent"))))
