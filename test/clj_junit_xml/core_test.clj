(ns clj-junit-xml.core-test
  (:require [clojure.test :refer :all]
            [clj-junit-xml.core :as xml]))

(use-fixtures :once xml/fixture)

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(deftest thrower
  (testing "Tests that throws"
    (is (pos? 6))
    (throw (ex-info "foo" {}))))

(deftest another-test
  (testing "Tests that pass"
    (is (pos? 6))))
