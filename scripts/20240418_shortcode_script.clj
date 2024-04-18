(ns partnorize-api
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [partnorize-api.data.buyerspheres :as bs]))

(defn get-buyerspheres-without-shortcode [db]
  (let [query (-> (h/select :id)
                  (h/from :buyersphere)
                  (h/where [:is :shortcode nil]))]
    (->> query
         (db/->>execute db))))

(defn add-shortcode-to-buyersphere [db id]
  (let [shortcode (bs/find-valid-shortcode db)
        query (-> (h/update :buyersphere)
                  (h/set {:shortcode shortcode})
                  (h/where [:= :id id]))]
    (->> query
         (db/->>execute db))))

(comment
  (get-buyerspheres-without-shortcode db/local-db)
  (add-shortcode-to-buyersphere db/local-db 2)

  (let [db {:dbtype "postgresql"
            :dbname "buyersphere"
            :host "buyersphere-prod.cc2idiull87l.us-east-2.rds.amazonaws.com"
            :user "postgres"
            :password "MUMlmURVCC9Wed9Lv79Pqi5a"
            :ssl false}
        bs (get-buyerspheres-without-shortcode db)]
    (doseq [{id :id} bs]
      (println id
       (add-shortcode-to-buyersphere db id))))
  ;
  )