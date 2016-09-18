(ns geoclojure.provider.google
  "A Google geocoding provider"
  (:require [geoclojure
             [query :as q]
             [provider :as p]]))

(declare results*)

(def provider
  (reify p/Provider
    (uri [_ q]
      (str
       "http://maps.googleapis.com/maps/api/geocode/json?"
       (if (q/reverse? q) "latlng=" "address=")
       (q/encode q)))
    (results [_ _ data]
      (results* (p/parse-json (:body data))))))

(defn filter-type
  "Filters response data by type"
  [data path types]
  {:pre [(map? data) (coll? path) (coll? types)]}
  (letfn [(pred [m] (some (set (:types m)) (map name types)))]
    (->> (get-in data path)
         (filter pred)
         (vec))))

(defn get-in-type
  "Gets the first instance of data for the given types"
  [data path types key]
  (-> (filter-type data path types)
      (first)
      (get key)))

(defn- result
  "Produces a result map"
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

(defn- results*
  [{:keys [results]}]
  (mapv result results))
