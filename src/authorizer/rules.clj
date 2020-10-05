(ns authorizer.rules)

(defn hasLimit?
  "checks if an account has sufficient limit to complete a transaction"
  [account transaction-amount]
  (true? (>= (:limit account) transaction-amount)))

(defn activeCard?
  "return true if a card is active
   otherwhise false"
   [account]
   (true? (:activeCard account)))