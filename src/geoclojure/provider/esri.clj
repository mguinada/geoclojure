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
       (if (q/reverse? q) "?location=" "?address=")
       (q/encode (if (q/reverse? q) (p/reverse-coords q) q))
       "&f=pjson&outFields=*"))
    (results [_ q data]
      (results* q (p/parse-json (:body data))))))

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

(defn- reverse-result
  [data]
  {:latitude
   (get-in data [:location :y])
   :longitude
   (get-in data [:location :x])
   :coordinates
   [(get-in data [:location :y]) (get-in data [:location :x])]
   :address
   (get-in data [:address :Match_addr])
   :city
   (get-in data [:address :City])
   :state
   (get-in data [:address :Region])
   :state-code
   (get-in data [:address :Region])
   :postal-code
   (get-in data [:address :Postal])
   :country
   (get-in data [:address :CountryCode])
   :country-code
   (get-in data [:address :CountryCode])})

(defn- results*
  [q {:keys [candidates] {:keys [code message]} :error :as response}]
  (if-not (p/http-error? code)
    (if (q/reverse? q) [(reverse-result response)] (mapv result candidates))
    (throw (ex-info
            (str "Provider Error " code)
            {:type :provider
             :status code
             :message message}))))
