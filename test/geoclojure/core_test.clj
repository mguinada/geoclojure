(ns geoclojure.core_test
  (:require [clojure.test :as test :refer :all]
            [geoclojure.core :as geo]))

(deftest query
  (testing "query as string"
    (is (geo/reverse? "38.7079236, -9.1357347"))
    (is (not (geo/reverse? "Some address"))))
  (testing "query as collection"
    (is (geo/reverse? [38.7075614 -9.137430199999999]))
    (is (not (geo/reverse? ["Praça do Comércio" "1100-148 Lisboa", "Portugal"]))))
  (testing "query encoding"
    (is (= "" (is (geo/encode ""))))
    (is (= "" (is (geo/encode []))))
    (is (= "38.7079236%2C+-9.1357347" (is (geo/encode "38.7079236, -9.1357347"))))
    (is (= "38.7079236%2C-9.1357347" (is (geo/encode ["38.7079236", "-9.1357347"]))))
    (is (= "Pra%C3%A7a+do+Com%C3%A9rcio%2C1100-148+Lisboa%2CPortugal"
           (is (geo/encode ["Praça do Comércio" "1100-148 Lisboa" "Portugal"]))))))

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
           (first (geo/search "38.7075614, -9.137430199999999")))))
  (testing "error handling"
    (testing "HTTP errors"
      (is (thrown? clojure.lang.ExceptionInfo (geo/search ""))))))

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
