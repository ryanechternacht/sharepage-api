(ns partnorize-api.data.utilities
  (:require [autoclave.core :as ac]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            [clojure.string :as str]
            [honey.sql.helpers :as h]
            [partnorize-api.db :as db]
            [java-time.api :as jt]))

(defn get-next-ordering-query
  "generates a query designed to be used as a subquery in an `insert into`
   statement for a table like `persona` or `pain_point` that will get the
   1 + the max ordering for the current org"
  ([table organization-id]
   (-> (h/select [[:raw "COALESCE(MAX(ordering), 0) + 1"]])
       (h/from table)
       (h/where [:= :organization_id organization-id])))
  ([table organization-id & other-wheres]
   (-> (h/select [[:raw "COALESCE(MAX(ordering), 0) + 1"]])
       (h/from table)
       (h/where (apply conj
                       [:and [:= :organization_id organization-id]]
                       other-wheres)))))

(comment
  (db/->format (get-next-ordering-query :persona 1))
  (db/->format (get-next-ordering-query :buyersphere_user_account 1 [:= :buyersphere-id 1] [:= :team "seller"]))
  (db/->>execute db/local-db (get-next-ordering-query :buyersphere_user_account -1))
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

(defn kebab-case [coll]
  (cske/transform-keys csk/->kebab-case-keyword coll))

(comment
  (kebab-case {:a_b 1 :c_d "hello"})
  (kebab-case [{:a_b 1 :c_d "hello"}
               {:a_c 3 :d_e "b"}])
  (kebab-case {:a_b {:cD :e_f}})
  ;
  )

(defn to-date-string 
  "Converts a inst (normally from the db) into a date string
   (e.g. '2020-02-03). This is normally for when the true value
   is a date, but it keeps getting converted into a datetime
   by honey, our db provider, etc."
  [dt]
  (-> dt
      jt/local-date
      .toString))

(def read-date-string jt/local-date)

(defn index-by
  "Takes a collection and returns a map of the result of applying
   f to each entry, mapped to the entry that matches the result. 
   Only 1 result will be returned for entry (the last processed). 
   Entries where the result of applying f to item returns nil are
   dropped. 
   
   Ex: turn a set of db results into a map of id -> the result
   (normally to build lookups for intermediate processing). 
   
   If the optional vf is supplied, this fn will be applied to applied
   to each value as it is mapped. E.g. `(index-by :id :id2 db-rows)` 
   will return a map of :id -> :id2 for rows with an :id value"
  ([f coll]
   (index-by f identity coll))
  ([f vf coll]
   (->> coll
        (group-by f)
        (reduce (fn [m [k v]]
                  (if k
                    (assoc m k (vf (first v)))
                    m))
                {}))))

(comment
  (index-by :crm_opportunity_id [{:id 20, :crm_opportunity_id "006Hs00001H8xaUIAR"}
                                 {:id 19 :crm_opportunity_id "abc123"}
                                 {:id 18 :crm_opportunity_id nil}
                                 {:id 17}])
  (index-by :crm_opportunity_id :id [{:id 20, :crm_opportunity_id "006Hs00001H8xaUIAR"}
                                     {:id 19 :crm_opportunity_id "abc123"}
                                     {:id 18 :crm_opportunity_id nil}
                                     {:id 17}])
  ;
  )