(ns partnorize-api.data.questions
    (:require [honey.sql.helpers :as h]
              [partnorize-api.db :as db]))

(def ^:private base-questions-query
  (-> (h/select :question.id :question.orbit_id 
                :question.organization_id :question.page
                :question.ordering :question.type
                :question.answer)
      (h/from :question)))

(defn get-questions-by-orbit [db orbit-id]
  (-> base-questions-query
      (h/where [:= :question.orbit_id orbit-id])
      (db/->execute db)))

(comment
  (get-questions-by-orbit db/local-db 1)
  ;
  )
