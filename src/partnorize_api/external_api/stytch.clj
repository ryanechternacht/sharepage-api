(ns partnorize-api.external-api.stytch
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [lambdaisland.uri :as uri]))

(def ^:private default-session-timeout_minutes (* 30 24 60))

(defn- make-stytch-call [url project secret body]
  (-> (http/post url
                 {:content-type :json
                  :basic-auth [project secret]
                  :body (json/generate-string body)
                  :accept :json
                  :as :json})))

(defn- make-stytch-link 
  "NOTE: do not include a leading / or you will overwrite prior path info"
  [base-url path]
  (str (uri/join base-url path)))

(defn authenticate-session
  "Authenticates a session wiith stytch. Returns the logged in
   user or nil if the session isn't valid"
  [{:keys [base-url project secret]}
   session-token]
  (try
    (-> (make-stytch-call (make-stytch-link base-url "sessions/authenticate")
                          project
                          secret
                          {:session_token session-token
                           :session_duration_minutes default-session-timeout_minutes})
        :body
        :member)
    (catch Exception _
      nil)))

(make-stytch-call (make-stytch-link "https://test.stytch.com/v1/b2b/" "sessions/authenticate")
                  "project-test-c060f396-9aeb-402a-9afa-44d7f5146262"
                  "secret-test-hzGink_qnd4SiZEBFLGkI0YAnBVS8TjGHQM="
                  {:session_token })

(defn authenticate-magic-link
  "Authenticates the magic link login attempt with stytch.
   Returns the a session identifier user or nil if the session isn't valid."
  [{:keys [base-url project secret]}
   magic-link-token]
  (try 
    (-> (make-stytch-call (make-stytch-link base-url "magic_links/authenticate")
                          project
                          secret
                          {:magic_links_token magic-link-token
                           :session_duration_minutes default-session-timeout_minutes})
        :body
        :session_token)
    (catch Exception _
      nil)))

(defn authenticate-oauth
  "Authenticates the magic link login attempt with stytch.
   Returns the a session identifier user or nil if the session isn't valid."
  [{:keys [base-url project secret]}
   auth-token]
  (try
    (-> (make-stytch-call (make-stytch-link base-url "oauth/authenticate")
                          project
                          secret
                          {:oauth_token auth-token
                           :session_duration_minutes default-session-timeout_minutes})
        :body
        :session_token)
    (catch Exception _
      nil)))

(defn send-magic-link-email
  "Sends the user a magic-email-link. Returns a truthy value if the
   email was sent and a falsey value if it wasn't"
  [{:keys [base-url project secret redirect-url]}
   user-email stytch-organization-id]
  (try
    (-> (make-stytch-call (make-stytch-link base-url "magic_links/email/login_or_signup")
                          project
                          secret
                          {:email_address user-email
                           :organization_id stytch-organization-id
                           :login_redirect_url redirect-url}))
    (catch Exception _
      nil)))
