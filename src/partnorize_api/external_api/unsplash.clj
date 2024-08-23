(ns partnorize-api.external-api.unsplash
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [partnorize-api.middleware.config :as config]))

(defn- reformat-for-app [unsplash-app unsplash-result]
  {:blurhash (-> unsplash-result :blur_hash)
   :url (str (-> unsplash-result :urls :full)
             "&w=2400&utm_source=" unsplash-app "&utm_medium=referral")
   :author {:name (-> unsplash-result :user :name)
            :link (-> unsplash-result :user :links :self)}})

(defn search-unsplash
  [{:keys [api-key app-name]} query]
  (let [url (cond-> "https://api.unsplash.com/search/photos?orientation=landscape&per_page=16"
              (not (str/blank? query)) (str "&query=" query))]
    (->> (http/get url
                   {:content-type :json
                    :as :json
                    :accept :json
                    :headers {:Authorization (str "Client-ID " api-key)}})
         :body
         :results
         (map #(reformat-for-app app-name %)))))

(comment
  (search-unsplash (:unsplash config/config) "office")
  ;
  )
