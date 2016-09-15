(ns geoclojure.core-test
  (:require [clojure.test :as test :refer :all]
            [org.senatehouse.expect-call :as x]
            [clj-http.lite.client :as http]
            [geoclojure.core :as geo]
            [geoclojure.provider :as p])
  (:import [java.net URLEncoder]))

(def provider
  (reify p/Provider
    (uri [_ q]
      (str
       "http://provider.example.org/gcode?q="
       (URLEncoder/encode q "UTF-8")))
    (results [_ _]
      [{:address "Address",
        :city "City",
        :coordinates [9.99 -9.99],
        :country "Country",
        :country-code "XY",
        :latitude 9.99
        :longitude -9.99
        :postal-code "1000",
        :state "State",
        :state-code "ST"}])))

(deftest search
  (testing "geocoding"
    (x/expect-call
     (http/get ["http://provider.example.org/gcode?q=Some+address" {:throw-exceptions false}])
     (is (= [{:address "Address",
              :city "City",
              :coordinates [9.99 -9.99],
              :country "Country",
              :country-code "XY",
              :latitude 9.99
              :longitude -9.99
              :postal-code "1000",
              :state "State",
              :state-code "ST"}]
            (geo/search provider "Some address")))))
  (testing "reverse geocoding"
    (x/expect-call
     (http/get ["http://provider.example.org/gcode?q=9.99%2C+-9.99" _])
     (is (= {:address "Address",
             :city "City",
             :coordinates [9.99 -9.99],
             :country "Country",
             :country-code "XY",
             :latitude 9.99
             :longitude -9.99
             :postal-code "1000",
             :state "State",
             :state-code "ST"}
            (first (geo/search provider "9.99, -9.99"))))))
  (testing "error handling"
    (testing "handle HTTP errors that are not handled by the provider"
      (x/expect-call
       (http/get ["http://provider.example.org/gcode?q=" _] {:status 400})
       (is (thrown-with-msg?
            clojure.lang.ExceptionInfo
            #"HTTP Error 400"
            (geo/search provider "")))))))
