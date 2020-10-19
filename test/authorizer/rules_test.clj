(ns authorizer.rules_test
  (:require [midje.sweet :refer :all]
            [authorizer.rules :as rules]
            [clj-time.core :as t]
            [authorizer.transaction_fixture :as trx-fixture]
            [authorizer.state_fixture :as state-fixture]))

(fact "is-account?"
  (fact "given map contains :account should return true"
    (rules/is-account? {:account {:availableLimit 100}}) => true)

  (fact "given map does not contains :account should return false"
    (rules/is-account? {:transaction {:merchant "bk"}}) => false))

(fact "is-transaction?"
  (fact "given map contains :transaction should return true"
    (rules/is-transaction? {:transaction {:amount 90}}) => true)

  (fact "given map does not contains :transaction should return false"
    (rules/is-account? {:transaction {:merchant "bk"}}) => false))

(fact "new-account"
  (fact "given state does not already contains an account should return an account with empty violations"
    (rules/new-account {} {:account {:active-card true
                                     :available-limit 100}}) => {:account {:active-card true
                                                                           :available-limit 100}
                                                                 :violations []
                                                                 :transactions []})

  (fact "given state contains an account should apply violation :account-already-initialized"
    (rules/new-account {:account {:active-card true
                                  :available-limit 100}}
                       {:account {:active-card true
                                  :available-limit 100}}) => {:account {:active-card true
                                                                        :available-limit 100}
                                                              :violations [:account-already-initialized]}))

(fact "has-limit?"
  (fact "given account available-limit >= transaction amount should return true"
    (rules/has-limit? {:available-limit 100} 90) => true)

  (fact "given account available-limit < transaction amount should return false"
    (rules/has-limit? {:available-limit 10} 90) => false))

(fact "active-card?"
  (fact "given card is active should return true"
    (rules/active-card? {:active-card true}) => true)

  (fact "given card is not active should return false"
    (rules/active-card? {:active-card false}) => false))

(fact "high-frequency-small-interval?"
  (fact "given there are 3 or more transactions in the last 2 minutes should return true"
    (rules/high-frequency-small-interval? trx-fixture/three-on-two-minutes-interval
                                          (first trx-fixture/three-on-two-minutes-interval)) => true)

  (fact "given there are less than 3 transactions in the last 2 minutes should return false"
    (rules/high-frequency-small-interval? trx-fixture/two-on-two-minutes-interval
                                          trx-fixture/one-regular) => false)

  (fact "given there are 5 transactions not in the last 2 minutes should return false"
    (rules/high-frequency-small-interval? trx-fixture/five-not-on-two-minutes-interval
                                          trx-fixture/one-regular) => false))

(fact "are-there-similar-transactions?"
  (fact "given there are 2 or more similar transactions"

    (fact "in sequence in the last 2 minutes should return true"
      (rules/are-there-similar-transactions? trx-fixture/two-similar-in-sequence-on-two-minutes-interval
                                             (first trx-fixture/two-similar-in-sequence-on-two-minutes-interval)) => true)

    (fact "not in sequence in the last 2 minutes should return true"
      (rules/are-there-similar-transactions? trx-fixture/two-similar-not-in-sequence-on-two-minutes-interval
                                             (last trx-fixture/two-similar-not-in-sequence-on-two-minutes-interval)) => true)

    (fact "not in the last 2 minutes should return false"
      (rules/are-there-similar-transactions? trx-fixture/two-similar-not-on-two-minutes-interval
                                             (last trx-fixture/two-similar-not-on-two-minutes-interval)) => false))

 (fact "given there are less than 2 similar transactions in the last 2 minutes should return false"
   (rules/are-there-similar-transactions? trx-fixture/three-not-similar
                                          trx-fixture/one-regular) => false))

(fact "execute-transaction"
  (fact "given that the account has limit >= transaction amount should apply the transaction"
    (rules/execute-transaction state-fixture/account-with-1mm-available-limit
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/account-with-1mm-available-limit
                                   (update :transactions conj trx-fixture/one-regular)))

  (fact "given that the account does not has limit >= transaction amount should apply violation :insufficient-limit"
    (rules/execute-transaction state-fixture/account-with-1-available-limit
                               {:transaction trx-fixture/with-10-amount})
                               =>
                               (-> state-fixture/account-with-1-available-limit
                                   (update :violations conj :insufficient-limit)))

  (fact "given that the account has an active-card should apply the transaction"
    (rules/execute-transaction state-fixture/account-with-active-card
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/account-with-active-card
                                   (update :transactions conj trx-fixture/one-regular)))

  (fact "given that the account has not an active-card should apply violation"
    (rules/execute-transaction state-fixture/account-with-inactive-card
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/account-with-inactive-card
                                   (update :violations conj :card-not-active)))

  (fact "given that the state does not have 3 transactions on two minute interval should apply transaction"
    (rules/execute-transaction state-fixture/three-transactions-not-on-two-minutes-interval
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/three-transactions-not-on-two-minutes-interval
                                   (update :transactions conj trx-fixture/one-regular)))

  (fact "given that the state has 3 transactions on two minutes interval should apply violation :high-frequency-small-interval"
    (rules/execute-transaction state-fixture/three-transactions-on-two-minutes-interval
                               {:transaction (-> state-fixture/three-transactions-on-two-minutes-interval :transactions last)})
                               =>
                               (-> state-fixture/three-transactions-on-two-minutes-interval
                                   (update :violations conj :high-frequency-small-interval)))

  (fact "given that the state doesn't have 2 similar transactions on two minutes interval should apply transaction"
    (rules/execute-transaction state-fixture/two-not-similar-transactions-on-two-minutes-interval
                               {:transaction (-> state-fixture/two-not-similar-transactions-on-two-minutes-interval :transactions last)})
                               =>
                               (-> state-fixture/two-not-similar-transactions-on-two-minutes-interval
                                   (update :transactions conj (-> state-fixture/two-not-similar-transactions-on-two-minutes-interval :transactions last))))

  (fact "given that the state has 2 similar transactions not on two minutes interval should apply transaction"
    (rules/execute-transaction state-fixture/two-similar-transactions-not-on-two-minutes-interval
                               {:transaction (-> state-fixture/two-similar-transactions-not-on-two-minutes-interval :transactions last)})
                               =>
                               (-> state-fixture/two-similar-transactions-not-on-two-minutes-interval
                                   (update :transactions conj (-> state-fixture/two-similar-transactions-not-on-two-minutes-interval :transactions last))))

  (fact "given that the state has 2 similar transactions on two minutes interval should apply violation :doubled-transaction"
    (rules/execute-transaction state-fixture/two-similar-transactions-on-two-minutes-interval
                               {:transaction (-> state-fixture/two-similar-transactions-on-two-minutes-interval :transactions last)})
                               =>
                               (-> state-fixture/two-similar-transactions-on-two-minutes-interval
                                   (update :violations conj :doubled-transaction)))

 (fact "should be able to apply more than one violation to the same transaction"
    (rules/execute-transaction state-fixture/multiple-violations-to-the-same-transaction
                               {:transaction (-> state-fixture/multiple-violations-to-the-same-transaction :transactions last)})
                               =>
                               (-> state-fixture/multiple-violations-to-the-same-transaction
                                   (update :violations conj :insufficient-limit :doubled-transaction))))

(fact "get-account-response"
  (fact "should return an account with no violations"
    (rules/get-account-response state-fixture/account-with-no-violations)
      => {:account {:active-card true :available-limit 10} :violations []})

  (fact "should return an account with one violation :insufficient-limit"
    (rules/get-account-response state-fixture/account-with-insufficient-limit-violation)
      => {:account {:active-card true :available-limit 0} :violations [:insufficient-limit]})

  (fact "should return an account with multiple violations :card-not-active :insufficient-limit :high-frequency-small-interval"
    (rules/get-account-response state-fixture/account-with-multiple-violations)
      => {:account {:active-card false :available-limit 0} :violations [:card-not-active :insufficient-limit :high-frequency-small-interval]}))

