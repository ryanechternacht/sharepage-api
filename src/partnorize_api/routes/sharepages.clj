(ns partnorize-api.routes.sharepages
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.buyersphere-templates :as d-templates]
            [partnorize-api.middleware.prework :as prework]
            [ring.util.http-response :as response]))

(def POST-sharepages-global-template
  (cpj/POST "/v0.1/sharepages/global-template" original-req
    (let [{:keys [prework-errors db config organization user body]}
          (prework/do-prework original-req (prework/ensure-is-org-member))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (let [new-body (-> body
                                    (assoc-in [:template-data :seller-first-name] (:first-name user))
                                    (assoc-in [:template-data :seller-organization] (:name organization)))
              new-swaypage
              (d-templates/create-sharepage-from-global-template-coordinator
               config
               db
               organization
               user
               new-body)]
          (response/ok new-swaypage))))))
