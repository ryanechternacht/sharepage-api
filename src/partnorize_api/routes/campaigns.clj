(ns partnorize-api.routes.campaigns
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [ring.util.http-response :as response]
            [partnorize-api.middleware.prework :as prework]
            [partnorize-api.data.utilities :as u]))

;; This is partly defensive, and partly because i'm just sticking
;; csv data into jsonb columns. We should eventually store the files in
;; s3 and process from their (and then we can up our limits)
(def csv-row-limit 1000)

(defn- build-csv-postwork [organization uuid csv-data]
  {[:csv-upload :create uuid]
   {:organization-id (:id organization)
    :uuid uuid
    :header-row (first csv-data)
    :data-rows (->> csv-data (drop 1) (take csv-row-limit) vec)
    :sample-rows (->> csv-data (drop 1) (take 4) vec)}})

(defn- build-campaign-postwork [organization uuid csv-uuid body]
  {[:campaign :create uuid]
   (-> (select-keys body [:title])
       (assoc :swaypage-template-id (parse-long (:template-id body)))
       (merge {:uuid uuid
               :organization-id (:id organization)
               :csv-upload-uuid csv-uuid}))})

(defn- create-campaign-result [organization uuid template sample-data body]
  (merge (select-keys body [:title])
         {:uuid uuid
          :sample-data sample-data
          :template template
          :organization-id (:id organization)}))

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
  
  (create-campaign-result {:id 1}
                          (u/uuid-v7)
                          {:id 1 :title "i'm a template"}
                          [["a" "b" "c"]
                           ["d" "e" "f"]
                           ["g" "h" "i"]
                           ["j" "k" "l"]]
                          {:title "hello world"
                           :template-id 3})
  ;
  )


;; This works pretty well, except we have to manually construct the 
;; return value, and there isn't an easy way to share it with the return
;; of the normal get for the same resource
(def POST-campaigns
  (cpj/POST "/v0.1/campaigns" [template-id :<< coerce/as-int :as original-req]
    (let [{:keys [prework-errors csv-data organization template params]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/read-csv-file)
                              (prework/ensure-and-get-swaypage-template template-id))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (let [campaign-uuid (u/uuid-v7)
              csv-uuid (u/uuid-v7)
              sample-data (->> csv-data (drop 1) (take 4))]
          (-> (create-campaign-result organization
                                      campaign-uuid
                                      sample-data
                                      template
                                      params)
              response/ok
              (update :postwork merge (build-csv-postwork organization
                                                          csv-uuid
                                                          csv-data))
              (update :postwork merge (build-campaign-postwork organization
                                                               campaign-uuid
                                                               csv-uuid
                                                               params))))))))
