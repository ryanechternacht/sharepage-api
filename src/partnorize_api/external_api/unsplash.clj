(ns partnorize-api.external-api.unsplash
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [partnorize-api.middleware.config :as config]))

(defn- pull-out-image-info [unsplash-result]
  {:blurhash (-> unsplash-result :blur_hash)
   :url (str (-> unsplash-result :urls :full) "&w=2400")
   :author {:name (-> unsplash-result :user :name)
            :link (-> unsplash-result :user :links :self)}})

(defn search-unsplash
  [{:keys [api-key]} query]
  (let [url (cond-> "https://api.unsplash.com/search/photos?orientation=landscape&per_page=16"
              (not (str/blank? query)) (str "&query=" query))]
    (->> (http/get url
                   {:content-type :json
                    :as :json
                    :accept :json
                    :headers {:Authorization (str "Client-ID " api-key)}})
         :body
         :results
         (map pull-out-image-info))))

(comment
  (search-unsplash (:unsplash config/config) "office")
  ;
  )
