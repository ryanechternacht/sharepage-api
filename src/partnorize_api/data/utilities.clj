(ns partnorize-api.data.utilities
  (:require [autoclave.core :as ac]
            [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn get-next-ordering-query
  "generates a query designed to be used as a subquery in an `insert into`
   statement for a table like `persona` or `pain_point` that will get the
   1 + the max ordering for the current org"
  [table organization-id]
  (-> (h/select [[:raw "COALESCE(MAX(ordering)) + 1"]])
      (h/from table)
      (h/where [:= :organization_id organization-id])))

(def ^:private sanitation-policy (ac/html-merge-policies :BLOCKS :FORMATTING :LINKS))

(def sanitize-html (partial ac/html-sanitize sanitation-policy))

(comment
  (db/->format (get-next-ordering-query :persona 1))
  (sanitize-html "<b>hello</b>")
  (sanitize-html "<script src=''></script><i>hello, world<i>")
  (sanitize-html "<div>hello, world</div><style>background: blue;</style>")
  ;
  )