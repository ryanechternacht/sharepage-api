(ns partnorize-api.middleware.organization
  (:require [clojure.string :as str]
            [partnorize-api.data.organizations :as d-org]
            [partnorize-api.data.utilities :as util]))

(defn- get-subdomain-from-headers [headers]
  (try
    (-> headers (get "host") (str/split #"\.") first)
    (catch Exception _
      nil)))

(defn- wrap-organization-impl [handler {db :db headers :headers :as request}]
  (if-let [subdomain (get-subdomain-from-headers headers)]
    (if-let [organization (util/camel-case (d-org/get-by-subdomain db subdomain))]
      (handler (assoc request :organization organization))
      (handler request))
    (handler request)))

; This form has the advantage that changes to wrap-debug-impl are
; automatically reflected in the handler (due to the lookup in `wrap-user`)
(defn wrap-organization [h] (partial #'wrap-organization-impl h))
