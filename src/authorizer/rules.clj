(ns authorizer.rules)

(defn hasLimit?
  "checks if an account has sufficient limit to complete a transaction"
  [account transaction-amount]
  (true? (>= (:limit account) transaction-amount)))