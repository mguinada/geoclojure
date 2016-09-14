(ns geoclojure.core
  (:require [clojure.string :as string]
            [cheshire.core :as json]
            [clj-http.lite.client :as http])
  (:import [java.net URLEncoder]))

(defprotocol Query
  "Geocoding query protocol"
  (reverse? [this] "Returns true for reverse geocoding queries")
  (encode [this] "Encodes the query for HTTP"))

(defn- encode-query
  [query]
  (URLEncoder/encode query "UTF-8"))

(extend-protocol Query
  String
  (reverse? [this]
    (boolean (re-matches #"^(\-?\d+(\.\d+)?),\s*(\-?\d+(\.\d+)?)$" this)))
  (encode [this]
    (encode-query this))
  clojure.lang.IPersistentCollection
  (reverse? [this]
    (and (= 2 (count this)) (every? float? this)))
  (encode [this]
    (encode-query (string/join "," this))))

(defn url
  "Assemble a url to query google geocoding service"
  [query]
  (str
   "http://maps.googleapis.com/maps/api/geocode/json?"
   (if (reverse? query) "latlng=" "address=")
   (encode query)))

(declare parse-response)

(defn search
  "Performs a geocoding search"
  [query]
  (let [response (http/get (url query)) status (get response "status")]
    (if-not (contains? (set (range 400 600)) status)
      (parse-response response)
      (throw (ex-info
              (str "HTTP Error " status)
              {:status status :message (parse-response response)})))))

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
  "Produce a geoclojure result map.
   Result maps in geoclojure always returns with the following keys:

   :latitude - float
   :longitude - float
   :coordinates - array of the above two in the form of [lat, lng]
   :address - string
   :city - string
   :state - string
   :state-code - string
   :postal-code - string
   :country - string
   :country-code - string"
  [data]
  {:latitude
   (get-in data [:geometry :location :lat])
   :longitude
   (get-in data [:geometry :location :lng])
   :coordinates
   [(get-in data [:geometry :location :lat]) (get-in data [:geometry :location :lng])]
   :address
   (get-in data [:formatted_address])
   :city
   (get-in-type data [:address_components] [:locality :sublocality :administrative_area_level_3 :administrative_area_level_2] :long_name)
   :state
   (get-in-type data [:address_components] [:administrative_area_level_1] :long_name)
   :state-code
   (get-in-type data [:address_components] [:administrative_area_level_1] :short_name)
   :postal-code
   (get-in-type data [:address_components] [:postal_code] :long_name)
   :country
   (get-in-type data [:address_components] [:country] :long_name)
   :country-code
   (get-in-type data [:address_components] [:country] :short_name)})

(defn- results
  [{:keys [results]}]
  (mapv result results))

(defn- parse-response
  [{:keys [body]}]
  (results (json/parse-string body true)))
