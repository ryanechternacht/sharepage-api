(ns partnorize-api.middleware.users
  (:require [partnorize-api.data.users :as d-users]
            [partnorize-api.data.utilities :as u]))

;; airplane coding
;; (defn- wrap-user-impl [handler {:keys [session db organization] :as request}]
;;   (handler (assoc request :user (d-users/get-by-email db
;;                                                       (:id organization)
;;                                                       "ryan@echternacht.org"))))

;; ;; TODO do something more useful with the session info (like link it to whatever info we have saved)
(defn- wrap-user-impl [handler {:keys [session db organization] :as request}]
  (if (:email_address session)
    (let [user (u/kebab-case
                (d-users/get-by-email db
                                      (:id organization)
                                      (:email_address session)))]
      (handler (assoc request :user user)))
    (handler request)))

; This form has the advantage that changes to wrap-debug-impl are
; automatically reflected in the handler (due to the lookup in `wrap-user`)
(defn wrap-user [h] (partial #'wrap-user-impl h))
