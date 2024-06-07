(ns partnorize-api.middleware.prework
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [partnorize-api.data.permission :as permission]
            [partnorize-api.data.buyerspheres :as d-buyerspheres]
            [partnorize-api.db :as db]
            [ring.util.http-response :as response]))

;; TODO find a way to create a new version of compojure macros that
;; will automatically call this stuff when passed in (vs. manual invocations)

;; TODO error handling. preworks should attach errors, but then how do 
;; they get read?

(defn do-prework [req & preworks]
  (reduce (fn [r prework]
            (prework r))
          req
          preworks))

(defn generate-error-response [prework-errors]
  (cond
    (some #(= (:code %) 401) prework-errors)
    (response/unauthorized)

    (some #(= (:code %) 404) prework-errors)
    (response/not-found)

    (some #(= (:code %) 400) prework-errors)
    (response/bad-request)

    :else
    (response/bad-request)))

(comment
  (do-prework {} #(assoc % :a 1) #(assoc % :b 2))
  ;
  )


;; preworks should return fns that take just the req and operate against it

(defn ensure-can-see-swaypage [swaypage-id]
  (fn [{:keys [db organization user] :as req}]
    (if (permission/is-buyersphere-visible? db organization swaypage-id user)
      req
      (update req :prework-errors conj {:code 401}))))

(comment
  ;; success
  ((ensure-can-see-swaypage 1)
   {:db partnorize-api.db/local-db
    :organization {:id 1,
                   :name "Stark",
                   :logo "/house_stark.png",
                   :subdomain "stark",
                   :domain "https://www.house-stark.com",
                   :stytch_organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"}

    :user {:email "ryan@echternacht.org",
           :first_name "ryan",
           :organization_id 1,
           :is_admin true,
           :team "seller",
           :id 1,
           :last_name "echternacht",
           :display_role "Narrator3",
           :image "https://lh3.googleusercontent.com/a/ACg8ocLb7wmHRVHusvY_yEvlkfatANDCukY8EzHewNKiFbyzgt4=s96-c",
           :buyersphere_role "admin"}})

  ;; failure (no user and swaypage doesn't exist)
  ((ensure-can-see-swaypage 0) {:db partnorize-api.db/local-db
                                :organization nil
                                :user nil})
  ;
  )

(defn ensure-can-see-swaypage-by-shortcode [shortcode]
  (fn [{:keys [db organization user] :as req}]
    (let [{:keys [id]} (d-buyerspheres/get-by-shortcode db (:id organization) shortcode)]
      (if (permission/is-buyersphere-visible? db organization id user)
        req
        (update req :prework-errors conj {:code 401})))))

(comment
  ;; success
  ((ensure-can-see-swaypage-by-shortcode "abc123")
   {:db partnorize-api.db/local-db
    :organization {:id 1,
                   :name "Stark",
                   :logo "/house_stark.png",
                   :subdomain "stark",
                   :domain "https://www.house-stark.com",
                   :stytch_organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"}
    :user {:email "ryan@echternacht.org",
           :first_name "ryan",
           :organization_id 1,
           :is_admin true,
           :team "seller",
           :id 1,
           :last_name "echternacht",
           :display_role "Narrator3",
           :image "https://lh3.googleusercontent.com/a/ACg8ocLb7wmHRVHusvY_yEvlkfatANDCukY8EzHewNKiFbyzgt4=s96-c",
           :buyersphere_role "admin"}})

  ;; failure (no user and swaypage doesn't exist)
  ((ensure-can-see-swaypage-by-shortcode "b2ifI2") {:db partnorize-api.db/local-db
                                                    :organization nil
                                                    :user nil})
  
  ;; swaypage doesn't exist
    ((ensure-can-see-swaypage-by-shortcode "abc124")
     {:db partnorize-api.db/local-db
      :organization  {:id 1,
                      :name "Stark",
                      :logo "/house_stark.png",
                      :subdomain "stark",
                      :domain "https://www.house-stark.com",
                      :stytch_organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"}
      :user {:email "ryan@echternacht.org",
             :first_name "ryan",
             :organization_id 1,
             :is_admin true,
             :team "seller",
             :id 1,
             :last_name "echternacht",
             :display_role "Narrator3",
             :image "https://lh3.googleusercontent.com/a/ACg8ocLb7wmHRVHusvY_yEvlkfatANDCukY8EzHewNKiFbyzgt4=s96-c",
             :buyersphere_role "admin"}})
  ;
  )


(defn ensure-is-org-member []
  (fn [{:keys [db organization user] :as req}]
    (if (permission/does-user-have-org-permissions? db organization user)
      req
      (update req :prework-errors conj {:code 401}))))

(comment
  ;; success
  ((ensure-is-org-member)
   {:db partnorize-api.db/local-db
    :organization {:id 1,
                   :name "Stark",
                   :logo "/house_stark.png",
                   :subdomain "stark",
                   :domain "https://www.house-stark.com",
                   :stytch_organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"}

    :user {:email "ryan@echternacht.org",
           :first_name "ryan",
           :organization_id 1,
           :is_admin true,
           :team "seller",
           :id 1,
           :last_name "echternacht",
           :display_role "Narrator3",
           :image "https://lh3.googleusercontent.com/a/ACg8ocLb7wmHRVHusvY_yEvlkfatANDCukY8EzHewNKiFbyzgt4=s96-c",
           :buyersphere_role "admin"}})

  ;; failure (no user and swaypage doesn't exist)
  ((ensure-is-org-member) {:db partnorize-api.db/local-db
                                :organization nil
                                :user nil})
  ;
  )

(defn ensure-and-get-swaypage [swaypage-id]
  (fn [{:keys [db organization] :as req}]
    (try
      (if-let [swaypage (d-buyerspheres/get-full-buyersphere db (:id organization) swaypage-id)]
        (assoc req :swaypage swaypage)
        (update req :prework-errors conj {:code 404}))
      ;; currently we throw exceptions on bad ids
      (catch Exception _
        (update req :prework-errors conj {:code 404})))))

(comment
  ;; success
  ((ensure-and-get-swaypage 1)
   {:db partnorize-api.db/local-db
    :organization {:id 1,
                   :name "Stark",
                   :logo "/house_stark.png",
                   :subdomain "stark",
                   :domain "https://www.house-stark.com",
                   :stytch_organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"}})
  ;; no swaypage
  ((ensure-and-get-swaypage 0)
   {:db partnorize-api.db/local-db
    :organization {:id 1,
                   :name "Stark",
                   :logo "/house_stark.png",
                   :subdomain "stark",
                   :domain "https://www.house-stark.com",
                   :stytch_organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"}})
  ;
  )

(defn ensure-and-get-swaypage-by-shortcode [shortcode]
  (fn [{:keys [db organization] :as req}]
    (if-let [swaypage (d-buyerspheres/get-by-shortcode db (:id organization) shortcode)]
      (assoc req :swaypage swaypage)
      (update req :prework-errors conj {:code 404}))))

(comment
  ;; success
  ((ensure-and-get-swaypage-by-shortcode "abc123")
   {:db partnorize-api.db/local-db
    :organization {:id 1,
                   :name "Stark",
                   :logo "/house_stark.png",
                   :subdomain "stark",
                   :domain "https://www.house-stark.com",
                   :stytch_organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"}})
  ;; no swaypage
  ((ensure-and-get-swaypage "abc124")
   {:db partnorize-api.db/local-db
    :organization {:id 1,
                   :name "Stark",
                   :logo "/house_stark.png",
                   :subdomain "stark",
                   :domain "https://www.house-stark.com",
                   :stytch_organization_id "organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781"}})
  ;
  )

;; TODO this does all reading right away. this probably isn't ideal long term
(defn read-csv-file []
  (fn [req]
    (try
      (with-open [file-data (-> req
                                :params
                                :file
                                :tempfile
                                io/reader)]
        (assoc req :csv-data (mapv identity (csv/read-csv file-data))))
      (catch Exception _
        (update req :prework-errors conj {:code 400})))))

(comment
  ((read-csv-file)
   {:file {:tempfile "resources/test-csv.csv"}})

  ((read-csv-file)
   {:file {:tempfile "resources/test-csv2.csv"}})
  ;
  )
