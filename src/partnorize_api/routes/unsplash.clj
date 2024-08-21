(ns partnorize-api.routes.unsplash
  (:require [compojure.core :as cpj]
            [partnorize-api.external-api.unsplash :as unsplash]
            [partnorize-api.middleware.prework :as prework]
            [ring.util.http-response :as response]))

(def GET-search-unpslash
  (cpj/GET "/v0.1/search-unsplash/:query" [query :as original-req]
    (let [{:keys [prework-errors config]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok (unsplash/search-unsplash (:unsplash config) query))))))
