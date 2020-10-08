(ns authorizer.rules
  (:require [clj-time.core :as t]))

(defn has-limit?
  "checks if an account has sufficient limit to complete a transaction"
  [account transaction-amount]
  (true? (>= (:limit account) transaction-amount)))

(defn active-card?
  "return true if a card is active otherwhise false"
   [account]
   (true? (:active-card account)))

(defn high-frequency-small-interval?
  [transactions]
  "return true if there are 3 or more transactions on a 2 minute interval"
  (let [interval-in-minutes 2
        high-frequency-transaction-count 3
        date-time-start (t/minus (t/now) (t/minutes interval-in-minutes))]
    (-> (filter #(t/within? date-time-start (t/now) (:time %)) transactions)
        count
        (> high-frequency-transaction-count))))
