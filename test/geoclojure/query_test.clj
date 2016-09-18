(ns geoclojure.query-test
  (:require [clojure.test :as test :refer :all]
            [geoclojure.query :as q]))

(deftest query
  (testing "query as string"
    (is (q/reverse? "38.7079236, -9.1357347"))
    (is (not (q/reverse? "Some address"))))
  (testing "query as collection"
    (is (q/reverse? [38.7075614 -9.137430199999999]))
    (is (not (q/reverse? ["Praça do Comércio" "1100-148 Lisboa", "Portugal"]))))
  (testing "sets can't be reverse queries"
    (is (not (q/reverse? #{38.7075614 -9.137430199999999}))))
  (testing "query encoding"
    (is (= "" (is (q/encode ""))))
    (is (= "" (is (q/encode []))))
    (is (= "38.7079236%2C+-9.1357347" (is (q/encode "38.7079236, -9.1357347"))))
    (is (= "38.7079236%2C-9.1357347" (is (q/encode ["38.7079236", "-9.1357347"]))))
    (is (= "Pra%C3%A7a+do+Com%C3%A9rcio%2C1100-148+Lisboa%2CPortugal"
           (is (q/encode ["Praça do Comércio" "1100-148 Lisboa" "Portugal"]))))))
