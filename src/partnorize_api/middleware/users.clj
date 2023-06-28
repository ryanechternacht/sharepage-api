(ns partnorize-api.middleware.users)
  ;; (:require [yardstick-api.data.users :as d-users]))

;; (defn- wrap-user-impl [handler {:keys [session db] :as request}]
;;   (if (:user-id session)
;;     (let [user (d-users/get-user-by-id db (:user-id session))]
;;       (handler (assoc request :user user)))
;;     (handler request)))

;; TODO do something more useful with the session info (like link it to whatever info we have saved)
(defn- wrap-user-impl [handler {:keys [session] :as request}]
  (if session
    (handler (assoc request :user session))
    (handler request)))

; This form has the advantage that changes to wrap-debug-impl are
; automatically reflected in the handler (due to the lookup in `wrap-user`)
(defn wrap-user [h] (partial #'wrap-user-impl h))
