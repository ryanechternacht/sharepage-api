(ns partnorize-api.data.questions
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(def ^:private base-questions-query
  (-> (h/select :question.id :question.buyersphere_id 
                :question.organization_id :question.page
                :question.ordering :question.type
                :question.question :question.answer)
      (h/from :question)))

(defn get-by-buyersphere [db buyersphere-id]
  (-> base-questions-query
      (h/where [:= :question.buyersphere_id buyersphere-id])
      (db/->execute db)))

(comment
  (get-by-buyersphere db/local-db 1)
  ;
  )
