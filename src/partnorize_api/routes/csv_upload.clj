(ns partnorize-api.routes.csv-upload
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

(defn- build-csv-postwork [organization-id uuid csv-data]
  {[:csv-upload :create uuid]
   {:organization-id organization-id
    :uuid uuid
    :header-row (first csv-data)
    :data-rows (->> csv-data (drop 1) (take csv-row-limit) vec)
    :sample-rows (->> csv-data (drop 1) (take 4) vec)}})

(comment
  (build-csv-postwork 1
                      (u/uuid-v7)
                      [["header1" "header2" "header3"]
                       ["a" "b" "c"]
                       ["d" "e" "f"]
                       ["g" "h" "i"]
                       ["j" "k" "l"]
                       ["m" "n" "o"]])
  ;
  )

;; This works pretty well, except we have to manually construct the 
;; return value, and there isn't an easy way to share it with the return
;; of the normal get for the same resource
(def POST-csv
  (cpj/POST "/v0.1/csv-upload/template-data/v1" original-req
    (let [{:keys [prework-errors csv-data organization]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/read-csv-file))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (let [uuid (u/uuid-v7)
              sample-data (->> csv-data (drop 1) (take 4))]
          (-> {:uuid uuid
               :sample-data sample-data
               :organization-id (:id organization)}
              response/ok
              (update :postwork merge (build-csv-postwork (:id organization)
                                                          uuid
                                                          csv-data))))))))
