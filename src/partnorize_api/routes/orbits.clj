(ns partnorize-api.routes.orbits
  (:require [compojure.core :refer [GET]]
            [compojure.coercions :refer [as-int]]
            [ring.util.response :refer [response]]
            [partnorize-api.data.orbits :as d-orbits]))

(def GET-orbits
  (GET "/v0.1/orbits/:id" [id :<< as-int :as {:keys [db]}]
    (response (d-orbits/get-full-orbit db id))))

;; (def GET-obstacles
;;   (GET "/v0.1/student/:student-id/obstacles"
;;     [student-id :<< as-int :as {:keys [user db language]}]
;;     (if (auth/has-student-access? db user student-id :read)
;;       (response (d-obstacles/get-by-student-id db language student-id))
;;       auth/unauthorized-response)))
