(ns authorizer.user_input_fixture
  (:require [authorizer.core :as core]
            [clojure.string :as string]))


(defn- args->user-input
  [args]
  (string/join "\n" args))

(def one-account
  (args->user-input ["{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"]))

(def two-accounts
  (args->user-input ["{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"
                     "{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"]))

(def insufficient-limit-for-one-transaction
  (args->user-input ["{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 110, \"time\": \"2019-02-13T10:00:00.000Z\" } }"]))

(def insufficient-limit-for-the-last-transaction
  (args->user-input ["{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 10, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 100, \"time\": \"2019-02-13T10:00:00.000Z\" } }"]))

(def card-not-active-to-transact
  (args->user-input ["{ \"account\": { \"activeCard\": false, \"availableLimit\": 100 } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 90, \"time\": \"2019-02-13T10:00:00.000Z\" } }"]))

(def four-transactions-on-two-minute-interval
  (args->user-input ["{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 10, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"mc\", \"amount\": 10, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"lojinha da skina\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"lojinha da rua de trás\", \"amount\": 30, \"time\": \"2019-02-13T10:00:00.000Z\" } }"]))

(def three-similar-transactions-on-two-minute-interval
  (args->user-input ["{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 10, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 10, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 10, \"time\": \"2019-02-13T10:00:00.000Z\" } }"]))

(def multiple-violation-in-diferent-transactions
 (args->user-input ["{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 10, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 10, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 10, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 100, \"time\": \"2019-02-13T10:00:00.000Z\" } }"]))

(def multiple-violations-in-the-same-transactions
 (args->user-input ["{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 35, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 30, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 33, \"time\": \"2019-02-13T10:00:00.000Z\" } }"
                     "{ \"transaction\": { \"merchant\": \"Burger King\", \"amount\": 30, \"time\": \"2019-02-13T10:00:00.000Z\" } }"]))

(def account-and-deny-list
  (args->user-input ["{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }"
                     "{ \"denyList\": [\"merchant-A\", \"merchant-B\"]}"]))