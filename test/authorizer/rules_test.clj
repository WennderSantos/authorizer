(ns authorizer.rules_test
  (:require [midje.sweet :refer :all]
            [authorizer.rules :as rules]
            [clj-time.core :as t]))

(fact "has-limit?"
  (fact "given account limit >= transaction amount should return true"
    (rules/has-limit? {:available-limit 100} 90) => true)

  (fact "given account limit < transaction amount should return false"
    (rules/has-limit? {:available-limit 10} 90) => false))

(fact "active-card?"
  (fact "given card is active should return true"
    (rules/active-card? {:active-card true}) => true)

  (fact "given card is not active should return false"
    (rules/active-card? {:active-card false}) => false))

(fact "high-frequency-small-interval?"
  (fact "given there are more than 3 transactions in the last 2 minutes should return true"
    (rules/high-frequency-small-interval? [{:time (t/now)}
                                        {:time (t/now)}
                                        {:time (t/now)}
                                        {:time (t/now)}]) => true

    (rules/high-frequency-small-interval? [{:time (t/minus (t/now) (t/minutes 1))}
                                        {:time (t/minus (t/now) (t/minutes 2))}
                                        {:time (t/minus (t/now) (t/minutes 2))}
                                        {:time (t/now)}]) => true)

  (fact "given there are less or equal to 3 transactions in the last 2 minutes should return false"
    (rules/high-frequency-small-interval? [{:time (t/minus (t/now) (t/minutes 4))}
                                        {:time (t/minus (t/now) (t/minutes 3))}
                                        {:time (t/minus (t/now) (t/minutes 5))}
                                        {:time (t/now)}]) => false

    (rules/high-frequency-small-interval? [{:time (t/minus (t/now) (t/minutes 20))}
                                        {:time (t/minus (t/now) (t/minutes 3))}
                                        {:time (t/minus (t/now) (t/minutes 2))}
                                        {:time (t/minus (t/now) (t/minutes 5))}
                                        {:time (t/now)}]) => false))

 (fact "are-there-similar-transactions?"
  (fact "given there are more than 2 similar transactions in the last 2 minutes should return true"
    (rules/are-there-similar-transactions? {:time (t/now) :merchant "Burger King" :amount 20}
                                           [{:time (t/now) :merchant "Burger King" :amount 20}
                                            {:time (t/now) :merchant "Burger King" :amount 20}
                                            {:time (t/now) :merchant "Burger King" :amount 20}
                                            {:time (t/now)}]) => true

    (rules/are-there-similar-transactions? {:time (t/now) :merchant "Burger King" :amount 20}
                                           [{:time (t/now) :merchant "Burger King" :amount 20}
                                            {:time (t/minus (t/now) (t/minutes 2)) :merchant "Burger King" :amount 20}
                                            {:time (t/now) :merchant "Burger King" :amount 20}
                                            {:time (t/now)}]) => true)

  (fact "given there are less or equal to 2 similar transactions in the last 2 minutes should return false"
    (rules/are-there-similar-transactions? {:time (t/now) :merchant "mc donalds" :amount 10}
                                           [{:time (t/now) :merchant "Burger King" :amount 20}
                                            {:time (t/now)}]) => false

    (rules/are-there-similar-transactions? {:time (t/now) :merchant "mc donalds" :amount 10}
                                           [{:time (t/now) :merchant "mc donalds" :amount 10}
                                            {:time (t/now) :merchant "mc donalds" :amount 10}]) => false

    (rules/are-there-similar-transactions? {:time (t/now) :merchant "mc donalds" :amount 10}
                                           [{:time (t/now) :merchant "mc donalds" :amount 10}
                                            {:time (t/now) :merchant "mc donalds" :amount 10}
                                            {:time (t/now) :merchant "mc donalds" :amount 20}]) => false

    (rules/are-there-similar-transactions? {:time (t/now) :merchant "mc donalds" :amount 10}
                                           [{:time (t/now) :merchant "mc donalds" :amount 10}
                                            {:time (t/now) :merchant "mc donalds" :amount 10}
                                            {:time (t/minus (t/now) (t/minutes 3)) :merchant "mc donalds" :amount 10}]) => false))