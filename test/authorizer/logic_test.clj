(ns authorizer.logic_test
  (:require [midje.sweet :refer :all]
            [authorizer.logic :as logic]
            [clj-time.core :as t]
            [authorizer.transaction_fixture :as trx-fixture]
            [authorizer.state_fixture :as state-fixture]))

(fact "is-account?"
  (fact "given map contains :account should return true"
    (logic/is-account? {:account {:availableLimit 100}}) => true)

  (fact "given map does not contains :account should return false"
    (logic/is-account? {:transaction {:merchant "bk"}}) => false))

(fact "is-deny-list?"
  (fact "given map contains :denyList should return true"
    (logic/is-deny-list? {:denyList ["merchant-A" "merchant-B"]}) => true)

  (fact "given map contains :denyList should return true"
    (logic/is-deny-list? {:account {:availableLimit 100}}) => false))

(fact "is-marchant-in-deny-list?"
  (fact "given merchant is in denyList should return true"
    (logic/is-merchant-in-deny-list? "merchant-A" ["merchant-A" "merchant-B"]) => true)

  (fact "given merchant is not in denyList should return false"
    (logic/is-merchant-in-deny-list? "merchant-foo" ["merchant-A" "merchant-B"]) => false))

(fact "add-deny-list"
  (logic/add-deny-list {:account {:activeCard true
                                  :availableLimit 100}
                        :violations []
                        :transactions []} {:denyList ["merchant-a" "merchant-b"]}) => {:account {:activeCard true
                                                                                     :availableLimit 100
                                                                                     :denyList ["merchant-a" "merchant-b"]}
                                                                           :violations []
                                                                           :transactions []})

(fact "is-transaction?"
  (fact "given map contains :transaction should return true"
    (logic/is-transaction? {:transaction {:amount 90}}) => true)

  (fact "given map does not contains :transaction should return false"
    (logic/is-account? {:transaction {:merchant "bk"}}) => false))

(fact "new-account"
  (fact "given state does not already contains an account should return an account with empty violations"
    (logic/new-account {} {:account {:activeCard true
                                     :availableLimit 100}}) => {:account {:activeCard true
                                                                           :availableLimit 100}
                                                                 :violations []
                                                                 :transactions []})

  (fact "given state contains an account should apply violation :account-already-initialized"
    (logic/new-account {:account {:activeCard true
                                  :availableLimit 100}}
                       {:account {:activeCard true
                                  :availableLimit 100}}) => {:account {:activeCard true
                                                                        :availableLimit 100}
                                                              :violations [:account-already-initialized]}))

(fact "has-limit?"
  (fact "given account availableLimit >= transaction amount should return true"
    (logic/has-limit? {:availableLimit 100} 90) => true)

  (fact "given account availableLimit < transaction amount should return false"
    (logic/has-limit? {:availableLimit 10} 90) => false))

(fact "activeCard?"
  (fact "given card is active should return true"
    (logic/activeCard? {:activeCard true}) => true)

  (fact "given card is not active should return false"
    (logic/activeCard? {:activeCard false}) => false))

(fact "high-frequency-small-interval?"
  (fact "given there are 3 or more transactions in the last 2 minutes should return true"
    (logic/high-frequency-small-interval? trx-fixture/three-on-two-minutes-interval
                                          (first trx-fixture/three-on-two-minutes-interval)) => true)

  (fact "given there are less than 3 transactions in the last 2 minutes should return false"
    (logic/high-frequency-small-interval? trx-fixture/two-on-two-minutes-interval
                                          trx-fixture/one-regular) => false)

  (fact "given there are 5 transactions not in the last 2 minutes should return false"
    (logic/high-frequency-small-interval? trx-fixture/five-not-on-two-minutes-interval
                                          trx-fixture/one-regular) => false))

(fact "are-there-similar-transactions?"
  (fact "given there are 2 or more similar transactions"
    (fact "in sequence in the last 2 minutes should return true"
      (logic/are-there-similar-transactions? trx-fixture/two-similar-in-sequence-on-two-minutes-interval
                                             (first trx-fixture/two-similar-in-sequence-on-two-minutes-interval)) => true)

    (fact "not in sequence in the last 2 minutes should return true"
      (logic/are-there-similar-transactions? trx-fixture/two-similar-not-in-sequence-on-two-minutes-interval
                                             (last trx-fixture/two-similar-not-in-sequence-on-two-minutes-interval)) => true)

    (fact "not in the last 2 minutes should return false"
      (logic/are-there-similar-transactions? trx-fixture/two-similar-not-on-two-minutes-interval
                                             (last trx-fixture/two-similar-not-on-two-minutes-interval)) => false))

 (fact "given there are less than 2 similar transactions in the last 2 minutes should return false"
   (logic/are-there-similar-transactions? trx-fixture/three-not-similar
                                          trx-fixture/one-regular) => false))

(fact "execute-transaction"
  (fact "given that the account has limit >= transaction amount should apply the transaction"
    (logic/execute-transaction state-fixture/account-with-1mm-availableLimit
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/account-with-1mm-availableLimit
                                   (update :transactions conj trx-fixture/one-regular)))

  (fact "given that the account does not has limit >= transaction amount should apply violation :insufficient-limit"
    (logic/execute-transaction state-fixture/account-with-1-availableLimit
                               {:transaction trx-fixture/with-10-amount})
                               =>
                               (-> state-fixture/account-with-1-availableLimit
                                   (update :violations conj :insufficient-limit)))

  (fact "given that the account has an activeCard should apply the transaction"
    (logic/execute-transaction state-fixture/account-with-activeCard
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/account-with-activeCard
                                   (update :transactions conj trx-fixture/one-regular)))

  (fact "given that the account has not an activeCard should apply violation"
    (logic/execute-transaction state-fixture/account-with-inactiveCard
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/account-with-inactiveCard
                                   (update :violations conj :card-not-active)))

  (fact "given that the state does not have 3 transactions on two minute interval should apply transaction"
    (logic/execute-transaction state-fixture/three-transactions-not-on-two-minutes-interval
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/three-transactions-not-on-two-minutes-interval
                                   (update :transactions conj trx-fixture/one-regular)))

  (fact "given that the state has 3 transactions on two minutes interval should apply violation :high-frequency-small-interval"
    (logic/execute-transaction state-fixture/three-transactions-on-two-minutes-interval
                               {:transaction trx-fixture/one-on-two-minutes-interval})
                               =>
                               (-> state-fixture/three-transactions-on-two-minutes-interval
                                   (update :violations conj :high-frequency-small-interval)))

  (fact "given that the state doesn't have 2 similar transactions on two minutes interval should apply transaction"
    (logic/execute-transaction state-fixture/two-not-similar-transactions-on-two-minutes-interval
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/two-not-similar-transactions-on-two-minutes-interval
                                   (update :transactions conj trx-fixture/one-regular)))

  (fact "given that the state has 2 similar transactions not on two minutes interval should apply transaction"
    (logic/execute-transaction state-fixture/two-similar-transactions-not-on-two-minutes-interval
                               {:transaction trx-fixture/one-regular})
                               =>
                               (-> state-fixture/two-similar-transactions-not-on-two-minutes-interval
                                   (update :transactions conj trx-fixture/one-regular)))

  (fact "given that the state has 2 similar transactions on two minutes interval should apply violation :doubled-transaction"
    (logic/execute-transaction state-fixture/two-similar-transactions-on-two-minutes-interval
                               {:transaction trx-fixture/one-similar-on-two-minutes-interval})
                               =>
                               (-> state-fixture/two-similar-transactions-on-two-minutes-interval
                                   (update :violations conj :doubled-transaction)))

 (fact "should be able to apply more than one violation to the same transaction"
    (logic/execute-transaction state-fixture/multiple-violations-to-the-same-transaction
                               {:transaction trx-fixture/one-with-multiple-violations})
                               =>
                               (-> state-fixture/multiple-violations-to-the-same-transaction
                                   (update :violations conj :insufficient-limit :card-not-active)))

 (fact "given a transaction with a merchant in denyList should apply violation :merchant-denied"
  (logic/execute-transaction state-fixture/account-with-deny-list
                             {:transaction {:merchant "merchant-a" :amount 10 :time "2020-02-13T10:00:00.000Z"}})
                             =>
                             (-> state-fixture/account-with-deny-list
                                 (update :violations conj :merchant-denied))))

(fact "get-account-response"
  (fact "should return an account with no violations"
    (logic/get-account-response state-fixture/account-with-no-violations)
      => {:account {:activeCard true :availableLimit 10} :violations []})

  (fact "should return an account with one violation :insufficient-limit"
    (logic/get-account-response state-fixture/account-with-insufficient-limit-violation)
      => {:account {:activeCard true :availableLimit 0} :violations [:insufficient-limit]})

  (fact "should return an account with multiple violations :card-not-active :insufficient-limit :high-frequency-small-interval"
    (logic/get-account-response state-fixture/account-with-multiple-violations)
      => {:account {:activeCard false :availableLimit 0} :violations [:card-not-active :insufficient-limit :high-frequency-small-interval]}))

