(ns partnorize-api.routes.swaypages
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [partnorize-api.data.buyer-session :as d-buyer-session]
            [partnorize-api.data.buyer-tracking :as d-buyer-tracking]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.data.buyersphere-activities :as d-buyer-activities]
            [partnorize-api.data.buyersphere-notes :as d-buyer-notes]
            [partnorize-api.data.buyersphere-resources :as d-buyer-res]
            [partnorize-api.data.conversations :as d-conversations]
            [partnorize-api.data.buyersphere-links :as d-links]
            [partnorize-api.data.buyersphere-pages :as d-pages]
            [partnorize-api.data.buyersphere-templates :as d-buyer-templates]
            [partnorize-api.data.permission :as d-permission]
            [partnorize-api.data.users :as d-users]
            [partnorize-api.data.teams :as d-teams]
            [partnorize-api.middleware.prework :as prework]
            [ring.util.http-response :as response]))

(def GET-swaypage
  (cpj/GET "/v0.1/swaypage/:id" [id :<< coerce/as-int :as original-req]
    (let [{:keys [prework-errors swaypage] :as req}
          (prework/do-prework original-req
                              (prework/ensure-can-see-swaypage id)
                              (prework/ensure-and-get-swaypage id))]
      (println "swaypage" swaypage)
      (if (seq prework-errors)
        (prework/generate-error-response req prework-errors)
        (response/ok swaypage)))))
