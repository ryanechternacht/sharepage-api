(ns partnorize-api.data.permission
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

;; TODO extend with the global admin stuff
(defn does-user-have-org-permissions? [db {o-id :id} {u-id :id}]
  (let [query (-> (h/select :id)
                  (h/from :user_account)
                  (h/where [:= :organization_id o-id]
                           [:= :id u-id]
                           [:= :buyersphere_role "admin"]))]
    (-> query
        (db/->execute db)
        count
        pos?)))

(comment
  (does-user-have-org-permissions? db/local-db {:id 1} {:id 1})
  (does-user-have-org-permissions? db/local-db {:id 1} {:id 4})
  (does-user-have-org-permissions? db/local-db {:id 3} {:id 1234})
  ;
  )

(defn- is-user-buyersphere-buyer? [db {o-id :id} buyersphere-id {u-id :id}]
  (let [query (-> (h/select :id)
                  (h/from :buyersphere_user_account)
                  (h/where [:= :organization_id o-id]
                           [:= :buyersphere_id buyersphere-id]
                           [:= :user_account_id u-id]
                           [:= :team "buyer"]))]
    (-> query
        (db/->execute db)
        count
        pos?)))

(defn does-user-have-buyersphere-view-permission [db organization buyersphere-id user]
  (or (does-user-have-org-permissions? db organization user)
      (is-user-buyersphere-buyer? db organization buyersphere-id user)))

(comment
  (is-user-buyersphere-buyer? db/local-db {:id 1} 1 {:id 4})
  (is-user-buyersphere-buyer? db/local-db {:id 1} 1 {:id 1})
  (is-user-buyersphere-buyer? db/local-db {:id 3} 123 {:id 1234})
  
  (does-user-have-buyersphere-view-permission db/local-db {:id 1} 1 {:id 4})
  (does-user-have-buyersphere-view-permission db/local-db {:id 1} 1 {:id 1})
  (does-user-have-buyersphere-view-permission db/local-db {:id 1} 2 {:id 4})
  (does-user-have-buyersphere-view-permission db/local-db {:id 3} 123 {:id 1234})
  
  ;
  )