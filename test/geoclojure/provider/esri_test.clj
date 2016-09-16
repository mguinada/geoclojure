(ns geoclojure.provider.esri-test
  (:require [clojure.test :as test :refer :all]
            [geoclojure.core :as geo]
            [geoclojure.provider.esri :as esri]))

(deftest search
  (testing "geocoding"
    (is (= {:address "Praça do Comércio, 1100-148, Lisboa",
            :city "Lisboa",
            :coordinates [38.70817937100048 -9.137347199999567],
            :country "PRT",
            :country-code "PRT",
            :latitude 38.70817937100048,
            :longitude -9.137347199999567,
            :postal-code "1100",
            :state "Lisboa",
            :state-code "Lisboa"}
           (first (geo/search esri/provider "Praça do Comércio, Lisbon")))))
  (testing "reverse geocoding"
    (is (= {:address "Praça do Comércio, 1100-148, Lisboa",
            :city "Lisboa",
            :coordinates [38.70817937100048 -9.137347199999567],
            :country "PRT",
            :country-code "PRT",
            :latitude 38.70817937100048,
            :longitude -9.137347199999567,
            :postal-code "1100",
            :state "Lisboa",
            :state-code "Lisboa"}
           (first (geo/search esri/provider "38.708179371000483, -9.137347199999567"))))))
