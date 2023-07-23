(ns partnorize-api.middleware.organization
  (:require [partnorize-api.data.organizations :as d-org]))

(defn- get-slug-from-headers [headers]
  (get headers "buyersphere-organization"))

(defn- wrap-organization-impl [handler {db :db headers :headers :as request}]
  (if-let [slug (get-slug-from-headers headers)]
    (if-let [organization (d-org/get-by-subdomain db slug)]
      (handler (assoc request :organization organization))
      (handler request))
    (handler request)))

; This form has the advantage that changes to wrap-debug-impl are
; automatically reflected in the handler (due to the lookup in `wrap-user`)
(defn wrap-organization [h] (partial #'wrap-organization-impl h))
