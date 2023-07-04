(ns partnorize-api.middleware.debug)

(defn- wrap-debug-impl [handler request]
  (let [response (handler request)]
    (println response)
    response))

(defn wrap-debug [h] (partial #'wrap-debug-impl h))