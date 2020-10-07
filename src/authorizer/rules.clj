(ns authorizer.rules
  (:require [clj-time.core :as t]))

(defn hasLimit?
  "checks if an account has sufficient limit to complete a transaction"
  [account transaction-amount]
  (true? (>= (:limit account) transaction-amount)))

(defn activeCard?
  "return true if a card is active
   otherwhise false"
   [account]
   (true? (:activeCard account)))

(defn highFrequencySmallInterval?
  [transactions]
  (let [intervalInMinutes 2
        highFrequencyParameterCount 3
        dateTimeInterval (t/minus (t/now) (t/minutes intervalInMinutes))]
    (-> (filter #(t/within? dateTimeInterval (t/now) (:time %)) transactions)
        count
        (> highFrequencyParameterCount))))
