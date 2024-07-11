(ns partnorize-api.routes.swaypages
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.middleware.prework :as prework]
            [ring.util.http-response :as response]))

(def GET-swaypages
  (cpj/GET "/v0.1/swaypages" original-req
    (let [{:keys [prework-errors db organization]}
          (prework/do-prework original-req 
                              (prework/ensure-is-org-member))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok (d-buyerspheres/get-by-organization db (:id organization)))))))

(def GET-swaypage
  (cpj/GET "/v0.1/swaypage/:id" [id :<< coerce/as-int :as original-req]
    (let [{:keys [prework-errors swaypage]}
          (prework/do-prework original-req
                              (prework/ensure-can-see-swaypage id)
                              (prework/ensure-and-get-swaypage id))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok swaypage)))))

(def GET-swaypage-by-shortcode
  (cpj/GET "/v0.1/swaypage/shortcode/:shortcode" [shortcode :as original-req]
    (let [{:keys [prework-errors swaypage]}
          (prework/do-prework original-req
                              (prework/ensure-can-see-swaypage-by-shortcode shortcode)
                              (prework/ensure-and-get-swaypage-by-shortcode shortcode))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok swaypage)))))

(def PATCH-swaypage
  (cpj/PATCH "/v0.1/swaypage/:id" [id :<< coerce/as-int :as original-req]
    (let [{:keys [prework-errors body]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/ensure-and-get-swaypage id))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (update (response/ok) :postwork conj [[:swaypage :update id] body])))))

;; users
;; chapters (pages)
;; links
;; sessions
;; template
