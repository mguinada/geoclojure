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
   (let [response (http/get (p/uri provider query) {:throw-exceptions false})]
     (p/results provider query response))))
