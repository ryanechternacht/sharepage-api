(ns partnorize-api.middleware.debug)

(defn- wrap-debug-impl [handler {:keys [request-method uri] :as request}]
  (println "request" request-method uri request)
  (let [response (handler request)]
    (println "response" request-method uri response)
    response))

(defn wrap-debug [h] (partial #'wrap-debug-impl h))
