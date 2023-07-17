(ns partnorize-api.routes.organization
  (:require [compojure.core :refer [GET]]
            [ring.util.http-response :as response]))

;; TODO do we need to auth this? probably?
(def GET-organization
  (GET "/v0.1/organization" {:keys [organization]}
    (if organization
      (response/ok organization)
      (response/not-found))))
