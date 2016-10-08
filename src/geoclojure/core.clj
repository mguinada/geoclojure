(ns geoclojure.core
  "Geoclojure API"
  (:require [clj-http.lite.client :as http]
            [geoclojure.provider :as p]))

(defn search
  "Performs a geocoding search."
  ([provider query]
   (let [response (http/get (p/uri provider query) {:throw-exceptions false})]
     (p/results provider query response))))
