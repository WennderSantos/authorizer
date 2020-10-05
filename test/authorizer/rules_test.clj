(ns authorizer.rules_test
  (:require [midje.sweet :refer :all]
            [authorizer.rules :as rules]))

(fact "hasLimit?"
  (fact "given account limit >= transaction amount should return true"
    (rules/hasLimit? {:limit 100} 90) => true)

  (fact "given account limit < transaction amount should return false"
    (rules/hasLimit? {:limit 10} 90) => false))

(fact "activeCard?"
  (fact "given card is active should return true"
    (rules/activeCard? {:activeCard true}) => true)

  (fact "given card is not active should return false"
    (rules/activeCard? {:activeCard false}) => false))