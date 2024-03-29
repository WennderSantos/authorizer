(ns authorizer.logic
  (:require [clj-time.core :as t]
            [clj-time.format :as f]))

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

(defn is-deny-list?
  [input]
  (contains? input :denyList))

(defn add-deny-list
  [state denyList]
  (update-in state [:account] merge denyList))

(defn- apply-violation
  [state violation]
  (update state :violations conj violation))

(defn new-account
  "if state does not have an account return a new account with empty violations
   otherwise apply violation for account already initialized"
  [state new-account]
  (if (contains? state :account)
    (apply-violation state :account-already-initialized)
    (-> state
        (conj new-account)
        (assoc :violations [] :transactions []))))

(defn has-limit?
  "return true if an account has sufficient limit to complete a transaction,
   false otherwise"
  [account transaction-amount]
  (true? (>= (:availableLimit account) transaction-amount)))

(defn activeCard?
  "return true if a card is active, false otherwise"
   [account]
   (true? (:activeCard account)))

(defn is-merchant-in-deny-list?
  [merchant deny-list]
  (contains? (set deny-list) merchant))

(defn- filter-transactions-between-dates
  [transactions from to]
  (filter #(t/within? from (f/parse to) (f/parse (:time %))) transactions))

(defn- two-minutes-ago-of-transaction
  "return datetime value of two two minutes ago from a transaction time"
  [{:keys [:time]}]
  (t/minus (f/parse time) (t/minutes 2)))

(defn high-frequency-small-interval?
  "return true if there are 3 or more transactions on a 2 minute interval,
   false otherwise"
  [transactions current-transaction]
  (let [high-frequency-transaction-count 3]
    (-> (filter-transactions-between-dates transactions (two-minutes-ago-of-transaction current-transaction) (:time current-transaction))
        count
        (>= high-frequency-transaction-count))))

(defn- similar-transactions?
  [tr1 tr2]
  (and (= (:amount tr1) (:amount tr2))
       (= (:merchant tr1) (:merchant tr2))))

(defn are-there-similar-transactions?
  "return true if current-transaction has similar values (amount and merchant)
   compared with transactions from the last two minutes,
   false otherwise"
  [transactions current-transaction]
  (let [similar-transactions-accepted 2]
    (->> (filter-transactions-between-dates transactions (two-minutes-ago-of-transaction current-transaction) (:time current-transaction))
         (filter #(similar-transactions? current-transaction %))
         count
         (<= similar-transactions-accepted))))

(defn- apply-limit-validation
  [state transaction]
  (if (has-limit? (:account state) (:amount transaction))
    state
    (apply-violation state :insufficient-limit)))

(defn- apply-activeCard-validation
  [state]
  (if (activeCard? (:account state))
    state
    (apply-violation state :card-not-active)))

(defn- apply-high-frequency-small-interval-validation
  [state transaction]
  (if (high-frequency-small-interval? (:transactions state) transaction)
    (apply-violation state :high-frequency-small-interval)
    state))

(defn- apply-similar-transactions-validation
  [state transaction]
  (if (are-there-similar-transactions? (:transactions state) transaction)
    (apply-violation state :doubled-transaction)
    state))

(defn- apply-deny-list-validation
  [state transaction]
  (if (is-merchant-in-deny-list? (:merchant transaction) (get-in state [:account :denyList]))
    (apply-violation state :merchant-denied)
    state))

(defn- update-availableLimit
  [state]
  (->> state
       (:transactions)
       (map :amount)
       (reduce +)
       (update-in state [:account :availableLimit] -)))

(defn- apply-validations
  [state transaction]
  (-> state
      (update-availableLimit)
      (assoc :violations [])
      (apply-limit-validation transaction)
      (apply-activeCard-validation)
      (apply-high-frequency-small-interval-validation transaction)
      (apply-similar-transactions-validation transaction)
      (apply-deny-list-validation transaction)))

(defn get-account-response
  [state]
  (-> state
      (update-availableLimit)
      (select-keys [:account :violations])))

(defn- apply-transaction
  [transaction state]
    (update state :transactions conj transaction))

(defn execute-transaction
  [state {:keys [transaction]}]
  (let [violations (:violations (apply-validations state transaction))
        updated-state (assoc state :violations violations)]
    (if (empty? violations)
      (apply-transaction transaction updated-state)
      updated-state)))