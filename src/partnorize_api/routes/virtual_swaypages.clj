(ns partnorize-api.routes.virtual-swaypages
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [ring.util.http-response :as response]
            [partnorize-api.data.buyer-session :as buyer-session]
            [partnorize-api.middleware.prework :as prework]))

(def GET-virtual-swaypage
  (cpj/GET "/v0.1/virtual-swaypage/:shortcode" [shortcode :as req]
    (let [{:keys [prework-errors virtual-swaypage template owner]}
          (prework/do-prework req
                              (prework/ensure-and-get-virtual-swaypage shortcode))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok {:template template
                      :virtual-swaypage virtual-swaypage
                      :owner owner})))))

(def POST-virtual-swaypage-session
  (cpj/POST "/v0.1/virtual-swaypage/:shortcode/session" [shortcode :as req]
    (let [{:keys [prework-errors db virtual-swaypage organization user anonymous-user]}
          (prework/do-prework req
                              (prework/ensure-and-get-virtual-swaypage shortcode))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok (buyer-session/start-virtual-swaypage-session db
                                                                   (:id organization)
                                                                   (:id user)
                                                                   (:id virtual-swaypage)
                                                                   anonymous-user))))))

(def POST-virtual-swaypage-session-timing
  (cpj/POST "/v0.1/virtual-swaypage/:shortcode/session/:id/timing"
    [shortcode id :<< coerce/as-int :as req]
    (let [{:keys [prework-errors db virtual-swaypage organization body]}
          (prework/do-prework req
                              (prework/ensure-and-get-virtual-swaypage shortcode)
                              (prework/ensure-virutal-swaypage-session shortcode id))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok (buyer-session/track-virtual-swaypage-time db
                                                                (:id organization)
                                                                id
                                                                (:id virtual-swaypage)
                                                                body))))))

(def POST-virtual-swaypage-session-event
  (cpj/POST "/v0.1/virtual-swaypage/:shortcode/session/:id/:page/event"
    [shortcode page id :<< coerce/as-int :as req]
    (let [{:keys [prework-errors db virtual-swaypage organization body]}
          (prework/do-prework req
                              (prework/ensure-and-get-virtual-swaypage shortcode)
                              (prework/ensure-virutal-swaypage-session shortcode id))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok (buyer-session/track-virtual-swaypage-event db
                                                                 (:id organization)
                                                                 id
                                                                 (:id virtual-swaypage)
                                                                 page
                                                                 body))))))
