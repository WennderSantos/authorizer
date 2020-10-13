(ns authorizer.rules
  (:require [clj-time.core :as t]))

(def empty-violations [])

(defn is-account?
  "return true if a map contains :account keyword
   false otherwise"
  [input]
  (contains? input :account))

(defn is-transaction?
  "return true if a map contains :transaction keyword
   false otherwise"
  [input]
  (contains? input :transaction))

(defn new-account
  "return a new account with empty violations"
  [active-card available-limit]
  {:account {:active-card active-card
             :available-limit available-limit}
   :violations empty-violations})

(defn has-limit?
  "return true if an account has sufficient limit to complete a transaction,
   false otherwise"
  [account transaction-amount]
  (true? (>= (:available-limit account) transaction-amount)))

(defn active-card?
  "return true if a card is active, false otherwise"
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
  "return true if there are 3 or more transactions on a 2 minute interval,
   false otherwise"
  [transactions]
  (let [high-frequency-transaction-count 3]
    (-> (filter-transactions-between-dates transactions (two-minutes-ago) (t/now))
        count
        (> high-frequency-transaction-count))))

(defn- similar-transactions?
  [tr1 tr2]
  (and (= (:amount tr1) (:amount tr2))
       (= (:merchant tr1) (:merchant tr2))))

(defn are-there-similar-transactions?
  "return true if current-transaction has similar values (amount and merchant)
   compared with transactions from the last two minutes,
   false otherwise"
  [current-transaction transactions]
  (let [similar-transactions-accepted 2
        transactions-from-last-two-minutes (filter-transactions-between-dates transactions (two-minutes-ago) (t/now))]
    (-> (filter #(similar-transactions? current-transaction %) transactions-from-last-two-minutes)
        count
        (> similar-transactions-accepted))))

