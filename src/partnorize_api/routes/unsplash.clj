(ns partnorize-api.routes.unsplash
  (:require [compojure.core :as cpj]
            [partnorize-api.external-api.unsplash :as unsplash]
            [partnorize-api.middleware.prework :as prework]
            [ring.util.http-response :as response]))

(def GET-search-unsplash
  (cpj/GET "/v0.1/search-unsplash/:query" [query :as original-req]
    (let [{:keys [prework-errors config]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok (unsplash/search-unsplash (:unsplash config) query))))))


;; The body is mising?
(def POST-download-unsplash
  (cpj/POST "/v0.1/download-unsplash" original-req
    (let [{:keys [prework-errors config body]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok (unsplash/unsplash-download (:unsplash config) (:link body)))))))
