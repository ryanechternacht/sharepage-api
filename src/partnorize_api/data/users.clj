(ns partnorize-api.data.users
  (:require  [honey.sql.helpers :as h]
             [partnorize-api.middleware.config :as config]
             [partnorize-api.db :as db]
             [partnorize-api.external-api.stytch :as stytch]
             [clojure.string :as str]))

(def ^:private user-columns
  [:user_account.id :user_account.email :user_account.buyersphere_role
   :user_account.display_role :user_account.organization_id
   :user_account.first_name :user_account.last_name
   :user_account.is_admin :user_account.image])

(defn- base-user-query [organization-id] 
  (-> (apply h/select user-columns)
      (h/from :user_account)
      (h/where [:= :user_account.organization_id organization-id])
      (h/order-by :first_name :last_name)))

;; TODO I don't love how global admin stuff has to pollute this 
;; to work correctly
(defn get-by-email [db organization-id email]
  (let [user-in-org-query (-> (base-user-query organization-id)
                              (h/where [:= :user_account.email email])
                              (select-keys [:select :from :where]))
        global-admin-query (-> (apply h/select user-columns)
                               (h/from :user_account)
                               (h/where [:= :user_account.email email]
                                        [:is :user_account.is_admin :true]))]
    (-> (h/union user-in-org-query
                 global-admin-query)
        (h/order-by [:is_admin :desc])
        (db/->execute db)
        first)))

(defn get-by-organization [db organization-id]
  (-> (base-user-query organization-id)
      (h/where [:= :user_account.buyersphere_role "admin"])
      (db/->execute db)))

(defn get-by-id [db organization-id id]
  (-> (base-user-query organization-id)
      (h/where [:= :user_account.id id])
      (db/->execute db)
      first))

(defn create-user [config db organization buyersphere-role {:keys [first-name last-name display-role email]}]
  (let [stytch-member-id (stytch/create-user (:stytch config)
                                             (:stytch-organization-id organization)
                                             email
                                             (str first-name " " last-name))]
    (stytch/send-magic-link-email (:stytch config)
                                  (:stytch-organization-id organization)
                                  email)
    (-> (h/insert-into :user_account)
        (h/columns :organization_id :buyersphere_role :first_name
                   :last_name :display_role :email :stytch_member_id)
        (h/values [[(:id organization) buyersphere-role first-name
                    last-name display-role email stytch-member-id]])
        (#(apply h/returning % user-columns))
        (db/->execute db)
        first)))

(defn update-user-from-stytch [db email name image]
  (try
    (let [updates (cond-> {}
                    (not (str/blank? name))
                    ((fn [m]
                       (let [pieces (str/split name #" ")
                             first-name (first pieces)
                             last-name (str/join " " (rest pieces))]
                         (merge m {:first_name first-name :last_name last-name}))))
                    (not (str/blank? image))
                    (merge {:image image}))]
      (-> (h/update :user_account)
          (h/set updates)
          (h/where [:= :email email])
          (db/->execute db)))
    (catch Exception _
         ;;  this isn't that important, so don't stop processing
      nil)))

(comment
  (get-by-email db/local-db 1 "ryan@echternacht.org")
  (get-by-email db/local-db 2 "admin@buyersphere.com")
  (get-by-id db/local-db 1 1)
  (get-by-organization db/local-db 1) ;; is_admin check
  (get-by-organization db/local-db 2)
  (create-user config/config
               db/local-db
               {:id 1 :stytch-organization-id "organization-test-bd2b29e6-8c0a-48e6-a1c4-d9689883785e"}
               "buyer"
               {:first-name "grace"
                :last-name "ooi"
                :email "grace11@echternacht.org"
                :display-role "my love"})
  (update-user-from-stytch db/local-db "ryan@echternacht.org" "bill nye the third" "https://www.google.com")
  ;
  )
