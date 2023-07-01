(ns partnorize-api.external-api.stytch
  (:require [cheshire.core :as json]
            [clj-http.client :as http]))

(def ^:private default-session-timeout_minutes (* 24 60))

(defn- make-stytch-call [url project secret body]
  (-> (http/post url
                 {:content-type :json
                  :basic-auth [project secret]
                  :body (json/generate-string body)
                  :accept :json
                  :as :json})))

(defn authenticate-session 
  "Authenticates a session wiith stytch. Returns the logged in
   user or nil if the session isn't valid"
  [{:keys [session-authenticate-url project secret]}
                            session-token]
  (try
    (-> (make-stytch-call session-authenticate-url
                          project
                          secret
                          {:session_token session-token
                           :session_duration_minutes default-session-timeout_minutes})
        :body
        :member)
    (catch Exception _
      nil)))

(defn authenticate-magic-link
  "Authenticates the magic link login attempt with stytch. 
   Returns the a session identifier user or nil if the session isn't valid"
  [{:keys [magic-link-authenticate-url project secret]}
   magic-link-token]
  (try 
    (-> (make-stytch-call magic-link-authenticate-url
                          project
                          secret
                          {:magic_links_token magic-link-token
                           :session_duration_minutes default-session-timeout_minutes})
        :body
        :session_token)
    (catch Exception _
      nil)))
