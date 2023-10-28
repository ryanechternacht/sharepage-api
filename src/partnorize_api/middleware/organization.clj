(ns partnorize-api.middleware.organization
  (:require [clojure.string :as str]
            [partnorize-api.data.organizations :as d-org]
            [partnorize-api.data.utilities :as u]))

(defn- get-subdomain-from-headers [headers]
  (try
    (-> headers (get "host") (str/split #"\.") first)
    (catch Exception _
      nil)))

(defn- wrap-organization-impl [handler {db :db headers :headers :as request}]
  (let [subdomain (get-subdomain-from-headers headers)
        organization (when subdomain
                       (u/kebab-case (d-org/get-by-subdomain db subdomain)))]
    (handler (cond-> request
               subdomain (assoc :subdomain subdomain)
               organization (assoc :organization organization)))))

; This form has the advantage that changes to wrap-debug-impl are
; automatically reflected in the handler (due to the lookup in `wrap-user`)
(defn wrap-organization [h] (partial #'wrap-organization-impl h))
