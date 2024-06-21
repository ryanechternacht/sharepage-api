(ns partnorize-api.server
  (:require [camel-snake-kebab.core :as csk]
            [partnorize-api.middleware.config :as m-config]
            [partnorize-api.middleware.db :as m-db]
            ;; [partnorize-api.middleware.debug :as m-debug]
            [partnorize-api.middleware.anonymous-users :as m-anon-users]
            [partnorize-api.middleware.organization :as m-org]
            [partnorize-api.middleware.kebabify-params :as m-kebabify-params]
            [partnorize-api.middleware.postwork :as m-postwork]
            [partnorize-api.middleware.stytch-store :as m-stytch]
            [partnorize-api.middleware.users :as m-users]
            [partnorize-api.routes :as r]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :as m-cors]
            [ring.middleware.json :as m-json]
            [ring.middleware.keyword-params :as m-keyword-param]
            [ring.middleware.multipart-params :as m-multi-params]
            [ring.middleware.params :as m-params]
            [ring.middleware.session :as m-session]))

(def session-store (m-stytch/stytch-store (:stytch m-config/config)
                                          (:pg-db m-config/config)))

(def handler
  (-> r/routes
      ;; m-debug/wrap-debug
      m-postwork/wrap-postwork
      m-kebabify-params/wrap-kebabify-params
      (m-json/wrap-json-body {:key-fn csk/->kebab-case-keyword})
      m-users/wrap-user
      m-anon-users/wrap-anonymous-user
      m-org/wrap-organization
      m-db/wrap-db
      m-config/wrap-config
      (m-session/wrap-session {:store session-store
                               :cookie-attrs (:cookie-attrs m-config/config)
                               :cookie-name "buyersphere-session"})
      m-keyword-param/wrap-keyword-params
      m-params/wrap-params
      m-multi-params/wrap-multipart-params
      m-json/wrap-json-response
      (m-cors/wrap-cors :access-control-allow-origin #".*"
                        :access-control-allow-methods [:get :patch :put :post :delete]
                        :access-control-allow-credentials "true")
      ;
      ))

;; TODO this is how we'll run background jobs
(defn job [params]
  0)

(defn webserver [params]
  (jetty/run-jetty #'handler params))

#_(webserver {:port 3001
              :join? false})

;; TRY THIS: https://clojurians.slack.com/archives/C03S1KBA2/p1706551970190269?thread_ts=1706543519.076899&cid=C03S1KBA2
;; (defonce web-server (atom nil))

;; (defn boot []
;;   (swap! web-server
;;          (fn [s]
;;            (when s (.stop s))
;;            (run-jetty #'app {:join? false :port 8080}))))

;; (comment

;;   (boot))
