(ns geoclojure.provider.google-test
  (:require [clojure.test :as test :refer :all]
            [geoclojure.provider :as p]))

(deftest reverse-coords
  (is (= "38.7081793,-9.1373471" (p/reverse-coords "-9.1373471,38.708179371")))
  (is (= '(38.708179371 -9.1373471) (p/reverse-coords '(-9.1373471 38.708179371)))))
