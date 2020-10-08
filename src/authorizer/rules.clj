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

(defn- filter-transactions-between-dates
  [transactions from to]
  (filter #(t/within? from to (:time %)) transactions))

(defn- two-minutes-ago
  "return datetime value of two two minutes ago"
  []
  (t/minus (t/now) (t/minutes 2)))

(defn high-frequency-small-interval?
  [transactions]
  "return true if there are 3 or more transactions on a 2 minute interval"
  (let [high-frequency-transaction-count 3]
    (-> (filter-transactions-between-dates transactions (two-minutes-ago) (t/now))
        count
        (> high-frequency-transaction-count))))

