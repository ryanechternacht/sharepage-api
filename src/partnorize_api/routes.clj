(ns partnorize-api.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [partnorize-api.routes.orbits :as orbits]
            [ring.util.response :refer [response redirect not-found]]))

(def GET-root-healthz
  (GET "/" []
    (response "I'm here")))

(def get-404
  (GET "*" []
    (not-found nil)))

(def post-404
  (POST "*" []
    (not-found nil)))

(defn set-session [response]
  ;; TODO is this secure? I think so?
  (println "new-set-session-3")
  (assoc response
         :session "muX4sF-iD2wdFxGO72mksanb3uyOaVtbur-H-098Czcs"))

(def GET-login
  (GET "/login" []
    (println "new-login-3")
    (set-session (response "login"))))

(def GET-login2
  (GET "/login2" []
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str "<h1>Hello World!</h1>")
     :session "I am a session. Fear me."}))

(defroutes routes
  #'GET-root-healthz
  #'orbits/GET-orbits
  #'GET-login
  #'GET-login2
  get-404
  post-404)
