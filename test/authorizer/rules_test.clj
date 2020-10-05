(ns authorizer.rules_test
  (:require [midje.sweet :refer :all]
            [authorizer.rules :as rules]))

(fact "hasLimit?"
  (fact "given account limit >= transaction amount should return true"
    (rules/hasLimit? {:limit 100} 90) => true)

  (fact "given account limit < transaction amount should return false"
    (rules/hasLimit? {:limit 10} 90) => false))