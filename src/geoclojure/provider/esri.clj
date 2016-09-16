(ns geoclojure.provider.esri
  "An esri geocoding provider"
  (:require [geoclojure
             [query :as q]
             [provider :as p]]))

(declare results*)

(def url
  "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/")

(def provider
  (reify p/Provider
    (uri [_ q]
      (str
       url
       (if (q/reverse? q) "reverseGeocode" "findAddressCandidates")
       "?f=json&outFields=*"
       (if (q/reverse? q) (throw (RuntimeException. "Unimplemented")) "&address=")
       (q/encode q)))
    (results [_ data]
      (results* (p/parse-json (:body data))))))

(defn- result
  [data]
  {:latitude
   (get-in data [:location :y])
   :longitude
   (get-in data [:location :x])
   :coordinates
   [(get-in data [:location :y]) (get-in data [:location :x])]
   :address
   (get data :address)
   :city
   (get-in data [:attributes :City])
   :state
   (get-in data [:attributes :Region])
   :state-code
   (get-in data [:attributes :Region])
   :postal-code
   (get-in data [:attributes :Postal])
   :country
   (get-in data [:attributes :Country])
   :country-code
   (get-in data [:attributes :Country])})

(defn- results*
  [{:keys [candidates]}]
  (mapv result candidates))
