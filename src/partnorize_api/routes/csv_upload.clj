(ns partnorize-api.routes.csv-upload
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [compojure.coercions :as coerce]
            [compojure.core :as cpj]
            [ring.util.http-response :as response]
            [partnorize-api.middleware.prework :as prework]))

(def POST-csv
  (cpj/POST "/v0.1/csv-upload/template-data/v1" original-req
    (let [{:keys [prework-errors csv-data]}
          (prework/do-prework original-req
                              (prework/ensure-is-org-member)
                              (prework/read-csv-file))]
      (if (seq prework-errors)
        (prework/generate-error-response prework-errors)
        (response/ok csv-data)))))

;; (def POST-csv
;;   (cpj/POST "/v0.1/csv" [file :as {:keys [config db user organization body]}]
;;     (with-open [file-data (-> file
;;                               :tempfile
;;                               io/reader)]
;;       (let [csv-data (drop 1 (csv/read-csv file-data))
;;             swaypage-data (map (fn [[_ first-name last-name company
;;                                      data-1 data-2 data-3]]
;;                                  {:first-name first-name
;;                                   :last-name last-name
;;                                   :company company
;;                                   :data-1 data-1
;;                                   :data-2 data-2
;;                                   :data-3 data-3})
;;                                csv-data)]
;;         (doseq [data swaypage-data]
;;           (templates/create-swaypage-from-template-coordinator
;;            config
;;            db
;;            (:id organization)
;;            132
;;            (:id user)
;;            {:template-data data
;;             :buyer (:company data)
;;             :buyer-logo ""})))
;;       (response/ok {}))))
