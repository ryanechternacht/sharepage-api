(ns partnorize-api.routes.auth
    (:require [compojure.core :refer [GET]]
              [ring.util.response :refer [response redirect bad-request]]
              [partnorize-api.data.organizations :as d-org]
              [partnorize-api.external-api.stytch :as stytch]))

(defn set-session [session_token response]
  ;; TODO is this secure? I think so?
  (assoc response
         :session session_token))

;; (def GET-login
;;   (GET "/login" []
;;     (println "new-login-3")
;;     (set-session (response "login"))))

;; (def GET-login2
;;   (GET "/login2" []
;;     {:status 200
;;      :headers {"Content-Type" "text/html"}
;;      :body (str "<h1>Hello World!</h1>")
;;      :session "I am a session. Fear me."}))

;; http://localhost:3000/login?
;;   slug=dunder-mifflin&
;;   stytch_token_type=multi_tenant_magic_links&
;;   token=f6IvHgJT7nsAq7hY8H05Va2CvHKDtkavbycyV5fhvn-w

(defn- magic-link-login [db {:keys [stytch]} slug token]
  (let [org (d-org/get-by-subdomain db slug)
        session_token (stytch/authenticate-magic-link stytch token)]
    (if (and org session_token)
      ;; TODO this should redirect to the right subdomain
      (set-session session_token (redirect "http://localhost:3000/"))
      (redirect "http://localhost:3000/login"))))

(def GET-login
  (GET "/v0.1/login" [slug stytch_token_type token :as {db :db config :config}]
    (condp = stytch_token_type
      "multi_tenant_magic_links" (magic-link-login db config slug token)
      (bad-request "Unknown stytch_token_type"))))

;; TODO add a link to generate the email
