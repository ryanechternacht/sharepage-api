(ns partnorize-api.routes.campaigns
  (:require [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [ring.util.http-response :as response]
            [ring.util.io :as ring-io]
            [partnorize-api.middleware.prework :as prework]
            [partnorize-api.data.campaigns :as campaigns]
            [partnorize-api.data.virtual-swaypages :as v-sp]
            [partnorize-api.data.utilities :as u]))

;; This is partly defensive, and partly because i'm just sticking
;; csv data into jsonb columns. We should eventually store the files in
;; s3 and process from their (and then we can up our limits)
(def csv-row-limit 500)

(defn- build-csv-postwork [organization uuid {:keys [data file-name]}]
  {[:csv-upload :create uuid]
   (let [data-rows (->> data (drop 1) (take csv-row-limit) vec)]
     {:organization-id (:id organization)
      :uuid uuid
      :file_name file-name
      :header-row (first data)
      :data-rows data-rows
      :data-rows-count (count (drop 1 data))
      :sample-rows (->> data (drop 1) (take 4) vec)})})

(defn- build-campaign-postwork [organization uuid csv-uuid body]
  {[:campaign :create uuid]
   (-> (select-keys body [:title])
       (assoc :swaypage-template-id (parse-long (:template-id body)))
       (merge {:uuid uuid
               :organization-id (:id organization)
               :csv-upload-uuid csv-uuid}))})

(defn- build-campaign-updates [body]
  (select-keys body [:title
                     :columns-approved
                     :ai-prompts-approved
                     :is-published]))

(comment
  (build-csv-postwork {:id 1}
                      (u/uuid-v7)
                      [["header1" "header2" "header3"]
                       ["a" "b" "c"]
                       ["d" "e" "f"]
                       ["g" "h" "i"]
                       ["j" "k" "l"]
                       ["m" "n" "o"]])

  (build-campaign-postwork {:id 1} (u/uuid-v7) (u/uuid-v7) {:title "hello world"
                                                            :template-id 3})
  ;
  )


;; This works pretty well, except we have to manually construct the 
;; return value, and there isn't an easy way to share it with the return
;; of the normal get for the same resource
(def POST-campaigns
  (cpj/POST "/v0.1/campaigns" [template-id :<< coerce/as-int :as original-req]
    (let [{:keys [prework-errors csv organization params]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/read-csv-file)
                              (prework/ensure-and-get-swaypage-template template-id))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (let [campaign-uuid (u/uuid-v7)
              csv-uuid (u/uuid-v7)]
          (-> (response/ok {:uuid (u/uuid->friendly-id campaign-uuid)})
              (update :postwork merge (build-csv-postwork organization
                                                          csv-uuid
                                                          csv))
              (update :postwork merge (build-campaign-postwork organization
                                                               campaign-uuid
                                                               csv-uuid
                                                               params))))))))

(def GET-campaign
  (cpj/GET "/v0.1/campaign/:uuid" [uuid :<< u/friendly-id->uuid :as original-req]
    (let [{:keys [prework-errors campaign]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/ensure-and-get-campaign uuid))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok campaign)))))

(def PATCH-campaign
  (cpj/PATCH "/v0.1/campaign/:uuid" [uuid :<< u/friendly-id->uuid :as original-req]
    (let [{:keys [prework-errors body]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/ensure-and-get-campaign uuid))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (update (response/ok (build-campaign-updates body)) :postwork conj [[:campaign :update uuid] body])))))

;; TODO I'd like to rework this to create virtual swaypages
;; instead of just the "publish" postwork. but it will take some
;; refactoring to get this done. see below for one
;; TODO we need a way for preworks to depend on other preworks
;; e.g. if i want to pull the template and pages related to campaign
;; i need to have pulled the campaign first, so i can get those ids

(def POST-campaign-publish
  (cpj/POST "/v0.1/campaign/:uuid/publish" [uuid :<< u/friendly-id->uuid :as original-req]
    (let [{:keys [prework-errors campaign]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/ensure-and-get-campaign uuid)
                              (prework/ensure-campaign-unpublished))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (-> (build-campaign-updates {:is-published true})
            response/ok
            (update :postwork
                    conj
                    [[:campaign :update uuid] {:is-published true}])
            (update :postwork
                    conj
                    [[:campaign :publish uuid] campaign]))))))

(def GET-campaign-published-csv
  (cpj/GET "/v0.1/campaign/:uuid/published-csv" [uuid :<< u/friendly-id->uuid :as original-req]
    (let [{:keys [prework-errors campaign db config organization]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/ensure-and-get-campaign uuid)
                              (prework/ensure-campaign-published))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (let [csv-data (campaigns/get-published-csv
                        config db organization uuid)]
          (-> (ring-io/piped-input-stream (fn [out]
                                            (with-open [writer (io/writer out)]
                                              (csv/write-csv writer csv-data))))
              response/ok
              (assoc-in [:headers "Content-Type"] "text/csv")
              (assoc-in [:headers "Content-Disposition"] 
                        (str "attachment; filename=\"" (:title campaign) ".csv\""))))))))

(def GET-campaigns
  (cpj/GET "/v0.1/campaigns" original-req
    (let [{:keys [prework-errors db organization]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok (campaigns/get-all db (:id organization)))))))

(def GET-campaign-swaypages
  (cpj/GET "/v0.1/campaign/:uuid/swaypages" [uuid :<< u/friendly-id->uuid :as original-req]
    (let [{:keys [prework-errors db organization]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/ensure-and-get-campaign uuid))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok (v-sp/get-virtual-swaypages-by-campaign db
                                                             (:id organization)
                                                             uuid))))))
