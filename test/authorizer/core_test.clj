(ns authorizer.core_test
  (:require [midje.sweet :refer :all]
            [authorizer.core :as core]
            [authorizer.user_input_fixture :as fixture]
            [clojure.string :as string]))

(defn- call-main-with
  [args]
  (-> (with-in-str args (core/-main))
      (with-out-str)
      (string/split-lines)))

(fact "-main"
  (fact "should create the account with availableLimit, activeCard set and empty violations"
    (call-main-with fixture/one-account) => ["{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"])

  (fact "once created, the account should not be updated or recreated"
    (call-main-with fixture/two-accounts) => ["{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
                                              "{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[\"account-already-initialized\"]}"])

  (fact "given one transaction, the transaction amount should not exceed available limit: insufficient-limit"
    (call-main-with fixture/insufficient-limit-for-one-transaction) => ["{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
                                                                        "{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[\"insufficient-limit\"]}"])

  (fact "given two transactions, the transaction amount should not exceed available limit: insufficient-limit"
    (call-main-with fixture/insufficient-limit-for-the-last-transaction) => ["{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
                                                                             "{\"account\":{\"active-card\":true,\"available-limit\":90},\"violations\":[]}"
                                                                             "{\"account\":{\"active-card\":true,\"available-limit\":90},\"violations\":[\"insufficient-limit\"]}"])

  (fact "no transaction should be accepted when the card is not active: card-not-active"
    (call-main-with fixture/card-not-active-to-transact) => ["{\"account\":{\"active-card\":false,\"available-limit\":100},\"violations\":[]}"
                                                             "{\"account\":{\"active-card\":false,\"available-limit\":100},\"violations\":[\"card-not-active\"]}"])

  (fact "there should not be more than 3 transactions on a 2 minute interval: high-frequency-small-interval"
    (call-main-with fixture/four-transactions-on-two-minute-interval) => ["{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
                                                                          "{\"account\":{\"active-card\":true,\"available-limit\":90},\"violations\":[]}"
                                                                          "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[]}"
                                                                          "{\"account\":{\"active-card\":true,\"available-limit\":60},\"violations\":[]}"
                                                                          "{\"account\":{\"active-card\":true,\"available-limit\":60},\"violations\":[\"high-frequency-small-interval\"]}"])

  (fact "there should not be more than 2 similar transactions (same amount and merchant) in a 2 minutes interval:"
    (call-main-with fixture/three-similar-transactions-on-two-minute-interval) => ["{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
                                                                                   "{\"account\":{\"active-card\":true,\"available-limit\":90},\"violations\":[]}"
                                                                                   "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[]}"
                                                                                   "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[\"doubled-transaction\"]}"])

  (fact "should be able to apply violation to diferent transactions"
    (call-main-with fixture/multiple-violation-in-diferent-transactions) => ["{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
                                                                             "{\"account\":{\"active-card\":true,\"available-limit\":90},\"violations\":[]}"
                                                                             "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[]}"
                                                                             "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[\"doubled-transaction\"]}"
                                                                             "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[\"insufficient-limit\"]}"])

(fact "should be able to apply multiple violations to the same transaction"
    (call-main-with fixture/multiple-violations-in-the-same-transactions) => ["{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
                                                                              "{\"account\":{\"active-card\":true,\"available-limit\":65},\"violations\":[]}"
                                                                              "{\"account\":{\"active-card\":true,\"available-limit\":35},\"violations\":[]}"
                                                                              "{\"account\":{\"active-card\":true,\"available-limit\":2},\"violations\":[]}"
                                                                              "{\"account\":{\"active-card\":true,\"available-limit\":2},\"violations\":[\"insufficient-limit\",\"high-frequency-small-interval\"]}"]))