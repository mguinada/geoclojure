(ns geoclojure.core
  "Geoclojure API"
  (:require [clj-http.lite.client :as http]
            [geoclojure.provider :as p]
            [geoclojure.provider.google :as g]))

(defn search
  "Performs a geocoding search."
  ([query]
   (search g/provider query))
  ([provider query]
   (let [response (http/get (p/uri provider query) {:throw-exceptions false})
         status (:status response)]
     (if-not (p/http-error? status)
       (p/results provider query response)
       (throw (ex-info
               (str "HTTP Error " status)
               {:type :http
                :status status
                :message (p/parse-json (:body response))}))))))
