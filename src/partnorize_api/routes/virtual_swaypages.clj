(ns partnorize-api.routes.virtual-swaypages
  (:require [compojure.core :as cpj]
            [ring.util.http-response :as response]
            [partnorize-api.middleware.prework :as prework]))

(def GET-virtual-swaypage
  (cpj/GET "/v0.1/virtual-swaypage/:shortcode" [shortcode :as original-req]
    (let [{:keys [prework-errors virtual-swaypage template owner]}
          (prework/do-prework original-req
                              (prework/ensure-and-get-virtual-swaypage shortcode))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok {:template template
                      :virtual-swaypage virtual-swaypage
                      :owner owner})))))
