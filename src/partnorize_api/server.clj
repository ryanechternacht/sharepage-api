(ns partnorize-api.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [partnorize-api.middleware.config :refer [wrap-config config]]
            [partnorize-api.middleware.db :refer [wrap-db]]
            [partnorize-api.middleware.organization :refer [wrap-organization]]
            [partnorize-api.middleware.stytch-store :refer [stytch-store]]
            [partnorize-api.middleware.users :refer [wrap-user]]
            [partnorize-api.routes :as r]))

(def session-store (stytch-store (:stytch config)))

(def handler
  (-> r/routes
      (wrap-json-body {:keywords? true})
      wrap-user
      wrap-organization
      wrap-db
      wrap-config
      (wrap-session {:store session-store :cookie-attrs (:cookie-attrs config)})
      wrap-params
      wrap-multipart-params
      wrap-json-response
      (wrap-cors :access-control-allow-origin #".*"
                 :access-control-allow-methods [:get :put :post :delete])))

(defn -main
  [& _]
  (run-jetty #'handler {:port 3001
                        :join? false}))

#_(-main)
