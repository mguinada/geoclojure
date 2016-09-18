(ns geoclojure.query
  "Geocoding query"
  (:require [clojure.string :as string])
  (:import [java.net URLEncoder]))

(defprotocol Query
  "Geocoding query"
  (reverse? [query] "Returns true for reverse geocoding queries")
  (encode [query] "Encode for HTTP"))

(defn- http-enc
  "Encode a query for HTTP"
  [query]
  (URLEncoder/encode query "UTF-8"))

(extend-protocol Query
  String
  (reverse? [q]
    (boolean
     (re-matches #"^(\-?\d+(\.\d+)?),\s*(\-?\d+(\.\d+)?)$" q)))
  (encode [q]
    (http-enc q))
  clojure.lang.IPersistentCollection
  (reverse? [q]
    (and (not (set? q))
         (= 2 (count q))
         (every? float? q)))
  (encode [q]
    (http-enc (string/join "," q))))
