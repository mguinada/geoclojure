(ns geoclojure.provider.google
  "A Google geocoding provider"
  (:require [geoclojure
             [query :as q]
             [provider :as p]]))

(declare results*)

(def ^:private errors
  {"OVER_QUERY_LIMIT" {:type :over-query-limit :msg "Query limit broken"}
   "REQUEST_DENIED" {:type :request-denied :msg "Request denied"}
   "INVALID_REQUEST" {:type :invalid-request :msg "Invalid request"}})

(defrecord Provider [key lang]
  p/Provider
  (uri [_ q]
    (str
     (if (nil? key) "http" "https")
     "://maps.googleapis.com/maps/api/geocode/json"
     (str "?language=" lang)
     (when-not (nil? key) (str "&key=" key))
     (if (q/reverse? q) "&latlng=" "&address=")
     (q/encode q)))
  (results [_ _ data]
    (results* (p/parse-json (:body data)))))

(defn provider
  ([] (provider {}))
  ([{:keys [key lang] :or {key nil lang "en-EN"}}]
   (->Provider key lang)))

(defn filter-type
  "Filters data by type"
  [data types]
  {:pre [(coll? data) (coll? types)]}
  (->> data
       (filter #(some (set (:types %)) (map name types)))
       (vec)))

(defn get-type
  "Gets the first instance of data for the given types"
  [data types key]
  (-> data
      (filter-type types)
      (first)
      (get key)))

(defn- result
  "Produces a result map"
  [{:keys [address_components] {:keys [location]} :geometry :as data}]
  {:latitude (:lat location)
   :longitude (:lng location)
   :coordinates [(:lat location) (:lng location)]
   :address (:formatted_address data)
   :city (get-type address_components [:locality :sublocality :administrative_area_level_3 :administrative_area_level_2] :long_name)
   :state (get-type address_components [:administrative_area_level_1] :long_name)
   :state-code (get-type address_components [:administrative_area_level_1] :short_name)
   :postal-code (get-type address_components [:postal_code] :long_name)
   :country (get-type address_components [:country] :long_name)
   :country-code (get-type address_components [:country] :short_name)})

(defn- results*
  [{:keys [results status]}]
  (if (= "OK" status)
    (mapv result results)
    (throw (ex-info
            (str "Provider Error " status)
            {:type :provider
             :status status
             :error (get-in errors [status :type])
             :message (get-in errors [status :msg])}))))
