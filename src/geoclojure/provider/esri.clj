(ns geoclojure.provider.esri
  "An esri geocoding provider"
  (:require [clojure.string :as string]
            [geoclojure
             [query :as q]
             [provider :as p]]))

(declare results*)

(def url "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/")

(defn reverse-coords
  [coords]
  {:pre [(or (coll? coords) (string? coords))]}
  (if (coll? coords)
    (reverse coords)
    (->> (string/split coords #",")
         (reverse)
         (map string/trim)
         (string/join ","))))

(def provider
  (reify p/Provider
    (uri [_ q]
      (str
       url
       (if (q/reverse? q) "reverseGeocode" "findAddressCandidates")
       (if (q/reverse? q) "?location=" "?address=")
       (q/encode (if (q/reverse? q) (reverse-coords q) q))
       "&f=pjson&outFields=*"))
    (results [_ q data]
      (results* q (p/parse-json (:body data))))))

(defn- result
  [{:keys [location attributes] :as data}]
  {:latitude (:y location)
   :longitude (:x location)
   :coordinates [(:y location) (:x location)]
   :address (:address data)
   :city (:City attributes)
   :state (:Region attributes)
   :state-code (:Region attributes)
   :postal-code (:Postal attributes)
   :country (:Country attributes)
   :country-code (:Country attributes)})

(defn- reverse-result
  [{:keys [location address] :as data}]
  {:latitude (:y location)
   :longitude (:x location)
   :coordinates [(:y location) (:x location)]
   :address (:Match_addr address)
   :city (:City address)
   :state (:Region address)
   :state-code (:Region address)
   :postal-code (:Postal address)
   :country (:CountryCode address)
   :country-code (:CountryCode address)})

(defn- unify-response
  [query response]
  (if (q/reverse? query)
    (vector (reverse-result response))
    (mapv result (:candidates response))))

(defn- results*
  [query {{:keys [code message]} :error :as response}]
  (if-not (p/http-error? code)
    (unify-response query response)
    (throw (ex-info
            (str "Provider Error " code)
            {:type :provider
             :status code
             :message message}))))
