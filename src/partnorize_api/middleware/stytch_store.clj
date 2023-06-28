(ns partnorize-api.middleware.stytch-store
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [ring.middleware.session.store :as rs]))

(deftype StytchStore [authenticate-url project secret]
  rs/SessionStore
  (read-session
    [_ key]
    ;; TODO pull this out?
    (-> (http/post authenticate-url
                   {:content-type :json
                    :basic-auth [project secret]
                    :body (json/generate-string
                           {:session_token key
                            :session_duration_minutes 1440})
                    :accept :json
                    :as :json})
        :body
        :member))
  (write-session
    [_ _ value]
    value)
  (delete-session
    [_ _]
  ;;  TODO?
    nil))

(defn stytch-store 
  "creates a store backed by stytch.com identity service"
  [{:keys [authenticate-url project secret]}]
  (StytchStore. authenticate-url project secret))
