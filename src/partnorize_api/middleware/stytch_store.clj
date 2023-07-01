(ns partnorize-api.middleware.stytch-store
  (:require [ring.middleware.session.store :as rs]
            [partnorize-api.external-api.stytch :as stytch]))

(deftype StytchStore [stytch-config]
  rs/SessionStore
  (read-session
    [_ session-token]
    ;; TODO pull this out?
    (stytch/authenticate-session stytch-config session-token))
  (write-session
    [_ _ value]
    value)
  (delete-session
    [_ _]
  ;;  TODO?
    nil))

(defn stytch-store 
  "creates a store backed by stytch.com identity service"
  [stytch-config]
  (StytchStore. stytch-config))