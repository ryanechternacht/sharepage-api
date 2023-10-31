(ns partnorize-api.migratus
  (:require [migratus.core :as migratus]
            [partnorize-api.middleware.config :as config]))

;; TODO how do I roll these out to AWS?
;; Ideally I'd port into an AWS repl. In reality I'll probably just
;; fire up a repl with config set to the right AWS environment?
(def db (:pg-db config/config))

;; TODO get rid of /migrations/schema folder
(def migratus-config {:store                :database
                      :migration-dir        "migrations/schema/"
                      :migration-table-name "migratus"
                      :db db})

;; TODO I need a better way to run migrations
;; ;; dev
;; (def db {:dbtype "postgresql"
;;           :dbname "buyersphere"
;;           :host "buyersphere.cc2idiull87l.us-east-2.rds.amazonaws.com"
;;           :user "postgres"
;;           :password "Z4L25#FDM#pe"
;;           :ssl false})

;; prod
;; (def db {:dbtype "postgresql"
;;          :dbname "buyersphere"
;;          :host "buyersphere-prod.cc2idiull87l.us-east-2.rds.amazonaws.com"
;;          :user "postgres"
;;          :password "MUMlmURVCC9Wed9Lv79Pqi5a"
;;          :ssl false})

(comment
  (migratus/create migratus-config "example")
  (migratus/migrate migratus-config)
  (migratus/rollback migratus-config)
  ;
  )

