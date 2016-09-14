(ns geoclojure.core
  (:require [clj-http.lite.client :as http]
            [cheshire.core :as json])
  (:import [java.net URLEncoder]))

;; result always has the following keys
;;
;; :latitude - float
;; :longitude - float
;; :coordinates - array of the above two in the form of [lat,lon]
;; :address - string
;; :city - string
;; :state - string
;; :state-code - string
;; :postal-code - string
;; :country - string
;; :country-code - string

;; TODO:
;; process response errors
;; add spec to responses

(defprotocol Query
  "THe geocoding query protocol"
  (reverse? [this] "Returns true in case of a reverse geocoding query"))

(extend-type String
  Query
  (reverse? [this]
    (boolean (re-matches #"^(\-?\d+(\.\d+)?),\s*(\-?\d+(\.\d+)?)$" this))))

(defn- encode-query
  [query]
  (URLEncoder/encode query "UTF-8"))

(defn url
  [query]
  (str
   "http://maps.googleapis.com/maps/api/geocode/json?"
   (if (reverse? query) "latlng=" "address=")
   (encode-query query)))

(declare parse-response)

(defn search
  "Performs a geocoding search"
  [query]
  (println "url" (url query))
  (let [response (http/get (url query))]
    (parse-response response)))

(defn filter-type
  "Filters response data by type"
  [data path types]
  {:pre [(map? data) (coll? path) (coll? types)]}
  (letfn [(pred [m] (some (set (:types m)) (map name types)))]
    (->> (get-in data path)
         (filter pred)
         (vec))))

(defn get-in-type
  "Gets the first instance of data of a given type"
  [data path types key]
  (-> (filter-type data path types)
      (first)
      (get key)))

(defn- result
  "Standartizes the result"
  [data]
  {:latitude (get-in data [:geometry :location :lat])
   :longitude (get-in data [:geometry :location :lng])
   :coordinates [(get-in data [:geometry :location :lat]) (get-in data [:geometry :location :lng])]
   :address (get-in data [:formatted_address])
   :city (get-in-type data [:address_components] [:locality :sublocality :administrative_area_level_3 :administrative_area_level_2] :long_name)
   :state (get-in-type data [:address_components] [:administrative_area_level_1] :long_name)
   :state-code (get-in-type data [:address_components] [:administrative_area_level_1] :short_name)
   :postal-code (get-in-type data [:address_components] [:postal_code] :long_name)
   :country (get-in-type data [:address_components] [:country] :long_name)
   :country-code (get-in-type data [:address_components] [:country] :short_name)})

(defn- results
  [{:keys [results]}]
  (mapv result results))

(defn- parse-response
  [{:keys [body]}]
  (results (json/parse-string body true)))
