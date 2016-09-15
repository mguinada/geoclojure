(ns geoclojure.query
  "Geocoding query"
  (:require [clojure.string :as string])
  (:import [java.net URLEncoder]))

(defprotocol Query
  "Geocoding query"
  (reverse? [query] "Returns true for reverse geocoding queries")
  (encode [query] "Encode for HTTP"))

(defn- httpq
  "Encode a query for HTTP"
  [query]
  (URLEncoder/encode query "UTF-8"))

(extend-protocol Query
  String
  (reverse? [query]
    (boolean
     (re-matches #"^(\-?\d+(\.\d+)?),\s*(\-?\d+(\.\d+)?)$" query)))
  (encode [query]
    (httpq query))
  clojure.lang.IPersistentCollection
  (reverse? [query]
    (and (= 2 (count query)) (every? float? query)))
  (encode [query]
    (httpq (string/join "," query))))
