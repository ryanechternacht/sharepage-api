(ns partnorize-api.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [partnorize-api.routes.orbits :as orbits]
            [partnorize-api.routes.auth :as auth]
            [ring.util.response :refer [response not-found]]))

(def GET-root-healthz
  (GET "/" []
    (response "I'm here")))

(def get-404
  (GET "*" []
    (not-found nil)))

(def post-404
  (POST "*" []
    (not-found nil)))

(defroutes routes
  #'GET-root-healthz
  #'orbits/GET-orbits
  #'auth/GET-login
  get-404
  post-404)
