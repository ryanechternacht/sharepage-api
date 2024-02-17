(ns partnorize-api.middleware.anonymous-users
  (:require [partnorize-api.data.utilities :as u]))

(defn- wrap-anonymous-user-impl [handler {:keys [cookies] :as req}]
  (handler (assoc req
                  :anonymous-user
                  {:linked-name (get-in cookies ["linked-name" :value])
                   :entered-name (get-in cookies ["entered-name" :value])})))

(defn wrap-anonymous-user [h] (partial #'wrap-anonymous-user-impl h))
