(ns partnorize-api.data.utilities
  (:require [autoclave.core :as ac]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            [clojure.string :as str]
            [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn get-next-ordering-query
  "generates a query designed to be used as a subquery in an `insert into`
   statement for a table like `persona` or `pain_point` that will get the
   1 + the max ordering for the current org"
  ([table organization-id]
   (-> (h/select [[:raw "COALESCE(MAX(ordering)) + 1"]])
       (h/from table)
       (h/where [:= :organization_id organization-id])))
  ([table organization-id & other-wheres]
   (-> (h/select [[:raw "COALESCE(MAX(ordering)) + 1"]])
       (h/from table)
       (h/where (apply conj
                       [:and [:= :organization_id organization-id]]
                       other-wheres)))))

(comment
  (db/->format (get-next-ordering-query :persona 1))
  (db/->format (get-next-ordering-query :buyersphere_user_account 1 [:= :buyersphere-id 1] [:= :team "seller"]))
  ;
  )

(def ^:private sanitation-policy (ac/html-merge-policies :BLOCKS :FORMATTING :LINKS))

(def sanitize-html (partial ac/html-sanitize sanitation-policy))

(comment
  (sanitize-html "<b>hello</b>")
  (sanitize-html "<script src=''></script><i>hello, world<i>")
  (sanitize-html "<div>hello, world</div><style>background: blue;</style>")
  ;
  )

(defn coerce-to-bool
  "'1', 'true', 'on' are coerced to true. everything else is nil"
  [str]
  (#{"1" "true" "on"} str))

(comment
  (coerce-to-bool "1")
  (coerce-to-bool "true")
  (coerce-to-bool "on")
  (coerce-to-bool "tru")
  (coerce-to-bool "0")
  ;
  )

(defn is-provided?
  "Used to evaluate if a query param is provided. 
   Returns false on nil, '', and false; returns true otherwise"
  [str]
  (cond 
    (nil? str) false
    (false? str) false
    (string? str) (not (str/blank? str))
    :else true))

(comment
  (is-provided? 0)
  (is-provided? false)
  (is-provided? "")
  (is-provided? "a")
  (is-provided? nil)
  (is-provided? "    ")
  ;
  )

(defn camel-case [coll]
  (cske/transform-keys csk/->kebab-case-keyword coll))

(comment
  (camel-case {:a_b 1 :c_d "hello"})
  (camel-case [{:a_b 1 :c_d "hello"}
               {:a_c 3 :d_e "b"}])
  ;
  )
