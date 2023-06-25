(ns partnorize-api.migratus
  (:require [migratus.core :as migratus]
            [partnorize-api.middleware.config :as config]))

;; TODO how do I roll these out to AWS?
;; Ideally I'd port into an AWS repl. In reality I'll probably just
;; fire up a repl with config set to the right AWS environment?
(def db (:pg-db config/config))

(def migratus-config {:store                :database
                      :migration-dir        "migrations/schema/"
                      :migration-table-name "migratus"
                      :db db})

(comment
  (migratus/create migratus-config "example")
  (migratus/migrate migratus-config)
  (migratus/rollback migratus-config)
  ;
  )
