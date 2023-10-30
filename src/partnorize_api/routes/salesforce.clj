(ns partnorize-api.routes.salesforce
  (:require [compojure.core :as cpj]
            [partnorize-api.external-api.salesforce :as sf]
            [ring.util.http-response :as response]))

;; TODO any/all auth
(def GET-opportunities
  (cpj/GET "/v0.1/salesforce/opportunities" [name :as {:keys [db]}]
    (response/ok (sf/query-opportunities))))
