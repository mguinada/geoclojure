(ns geoclojure.core_test
  (:require [clojure.test :as test :refer :all]
            [geoclojure.core :as geo]))

(deftest search
  (testing "geocoding"
    (is (= [{:address "Praça do Comércio, 1100-148 Lisboa, Portugal",
             :city "Lisboa",
             :coordinates [38.7075614 -9.137430199999999],
             :country "Portugal",
             :country-code "PT",
             :latitude 38.7075614,
             :longitude -9.137430199999999,
             :postal-code "1100-148",
             :state "Lisboa",
             :state-code "Lisboa"}]
           (geo/search "Praça do Comércio, Lisbon"))))
  (testing "reverse geocoding"
    (is (= {:address "Praça do Comércio MB, 1100-083 Lisboa, Portugal"
            :coordinates [38.7079236 -9.1357347]
            :state-code "Lisboa"
            :city "Lisboa"
            :country-code "PT"
            :longitude -9.1357347
            :state "Lisboa"
            :postal-code "1100-083"
            :latitude 38.7079236
            :country "Portugal"}
           (first (geo/search "38.7075614, -9.137430199999999"))))))

(deftest filter-type
  (let [response {:components [{:value "Praça do Comércio" :types ["route"]}
                               {:value "Lisboa" :types ["locality" "political"]}
                               {:value "Lisboa" :types ["political"]}
                               {:value "Portugal" :types ["country" "political"]}
                               {:value "1100-148" :types ["postal_code"]}
                               {:value "1100" :types ["postal_code" "postal_code_prefix"]}]}]
    (is (= [{:value "1100-148" :types ["postal_code"]}
            {:value "1100" :types ["postal_code" "postal_code_prefix"]}]
           (geo/filter-type response [:components] ["postal_code"])))
    (is (= [{:value "Praça do Comércio" :types ["route"]}
            {:value "Portugal" :types ["country" "political"]}]
           (geo/filter-type response [:components] [:country :route])))))
