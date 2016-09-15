(ns geoclojure.core
  "Geoclojure API"
  (:require [cheshire.core :as json]
            [clj-http.lite.client :as http]
            [geoclojure.provider :as p]
            [geoclojure.provider.google :as g]))

(defn- http-error?
  [{:keys [status]}]
  (contains? (set (range 400 600)) status))

(defn search
  "Performs a geocoding search."
  ([query]
   (search g/provider query))
  ([provider query]
   (let [response (http/get (p/uri provider query) {:throw-exceptions false})]
     (if-not (http-error? response)
       (p/results provider response)
       (throw (ex-info
               (str "HTTP Error " (:status response))
               {:type :http
                :status (:status response)
                :message (p/parse-json (:body response))}))))))
