(ns geoclojure.provider
  "A protocol for the geocoding service providers"
  (:require [cheshire.core :as json]))

(defprotocol Provider
  "A protocol that tells `geoclojure.core` how to interact with geocoding service providers."
  (uri [this query]
    "A ready to use URI for querying the service")
  (results [this data]
    "Returns the query results.
     Geoclojure result maps always contain the following keys:

     :latitude - float
     :longitude - float
     :coordinates - array of the above two in the form of [lat, lng]
     :address - string
     :city - string
     :state - string
     :state-code - string
     :postal-code - string
     :country - string
     :country-code - string"))

(defn parse-json
  [json]
  (json/parse-string json true))
