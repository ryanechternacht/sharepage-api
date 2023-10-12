(ns partnorize-api.data.permission
  (:require [honey.sql.helpers :as h]
            [partnorize-api.db :as db]))

(defn is-user-global-admin? [db {id :id}]
  (let [query (-> (h/select :id)
                  (h/from :user_account)
                  (h/where [:= :id id]
                           [:is :is_admin :true]))]
    (-> query
        (db/->execute db)
        seq)))

(comment
  (is-user-global-admin? db/local-db {:id 16})
  (is-user-global-admin? db/local-db {:id 1})
  (is-user-global-admin? db/local-db {:id 160})
  ;
  )

(defn is-user-buyersphere-seller? [db {o-id :id} {u-id :id}]
  (-> (h/select :id)
      (h/from :user_account)
      (h/where [:= :organization_id o-id]
               [:= :id u-id]
               [:= :buyersphere_role "admin"])
      (db/->execute db)
      seq))

(defn does-user-have-org-permissions? [db organization user]
  (or (is-user-buyersphere-seller? db organization user)
      (is-user-global-admin? db user)))

(comment
  (is-user-buyersphere-seller? db/local-db {:id 1} {:id 1})
  (is-user-buyersphere-seller? db/local-db {:id 1} {:id 4})
  (is-user-buyersphere-seller? db/local-db {:id 3} {:id 1234})

  (does-user-have-org-permissions? db/local-db {:id 1} {:id 1})
  (does-user-have-org-permissions? db/local-db {:id 1} {:id 4})
  (does-user-have-org-permissions? db/local-db {:id 3} {:id 1234})

  ;; admin checks
  (is-user-buyersphere-seller? db/local-db {:id 2} {:id 16})
  (does-user-have-org-permissions? db/local-db {:id 2} {:id 16})
  ;
  )

(defn- is-user-buyersphere-buyer? [db {o-id :id} buyersphere-id {u-id :id}]
  (-> (h/select :id)
      (h/from :buyersphere_user_account)
      (h/where [:= :organization_id o-id]
               [:= :buyersphere_id buyersphere-id]
               [:= :user_account_id u-id]
               [:= :team "buyer"])
      (db/->execute db)
      seq))

(defn can-user-see-buyersphere [db organization buyersphere-id user]
  (or (does-user-have-org-permissions? db organization user) ;; includes global admins
      (is-user-buyersphere-buyer? db organization buyersphere-id user)))

(defn- is-user-buyer? [db {o-id :id} {u-id :id}]
  (-> (h/select :id)
      (h/from :buyersphere_user_account)
      (h/where [:= :organization_id o-id]
               [:= :user_account_id u-id]
               [:= :team "buyer"])
      (db/->execute db)
      seq))

;; TODO we should probably merge this with can-user-see-buyersphere
(defn can-user-see-anything? [db organization user]
  (or (does-user-have-org-permissions? db organization user) ;; includes global admins
      (is-user-buyer? db organization user)))

(comment
  (is-user-buyersphere-buyer? db/local-db {:id 1} 1 {:id 4})
  (is-user-buyersphere-buyer? db/local-db {:id 3} 123 {:id 1234})

  (can-user-see-buyersphere db/local-db {:id 1} 1 {:id 4})
  (can-user-see-buyersphere db/local-db {:id 1} 2 {:id 4})
  (can-user-see-buyersphere db/local-db {:id 3} 123 {:id 1234})

  ;; seller checks
  (is-user-buyersphere-buyer? db/local-db {:id 1} 1 {:id 1})
  (can-user-see-buyersphere db/local-db {:id 1} 1 {:id 1})

  ;; admin checks
  (is-user-buyersphere-buyer? db/local-db {:id 2} 1 {:id 16})
  (can-user-see-buyersphere db/local-db {:id 2} 1 {:id 16})
  ;
  )