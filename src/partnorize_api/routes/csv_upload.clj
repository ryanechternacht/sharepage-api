(ns partnorize-api.routes.csv-upload
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [ring.util.http-response :as response]
            [partnorize-api.middleware.prework :as prework]
            [partnorize-api.data.utilities :as u]))

(defn- build-csv-postwork [organization-id csv-uuid csv-data]
  {[:csv-upload :create csv-uuid]
   {:organization-id 1
    :uuid csv-uuid}
   [:csv-upload-rows :create csv-uuid]
   (map-indexed (fn [i row]
                  {:organization-id organization-id
                   :csv-upload-uuid csv-uuid
                   :row-number (inc i)
                   :row-data row})
                (drop 1 csv-data))})

(comment
  (build-csv-postwork 1
                      (u/uuid-v7)
                      [["header1" "header2" "header3"]
                       ["a" "b" "c"]
                       ["d" "e" "f"]])
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
