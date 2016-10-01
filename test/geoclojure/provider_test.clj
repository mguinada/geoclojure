(ns geoclojure.provider-test
  (:require [clojure.test :as test :refer :all]
            [geoclojure.provider :as p]))

(def provider
  (reify p/Provider
    (uri [_ _] "http://geocoder.example.org/api/search")
    (results [_ _ _] [])))

(deftest register
  (testing "provider registry"
    (p/register provider)
    (is (= provider (p/registry :provider)))))
