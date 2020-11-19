(ns authorizer.state_fixture)

(def account-with-1mm-availableLimit
  {:account {:availableLimit 1000000 :activeCard true}
   :transactions []
   :violations []})

(def account-with-1-availableLimit
  {:account {:availableLimit 1 :activeCard true}
   :transactions []
   :violations []})

(def account-with-activeCard
  {:account {:availableLimit 20 :activeCard true}
   :transactions []
   :violations []})

(def account-with-inactiveCard
  {:account {:availableLimit 20 :activeCard false}
   :transactions []
   :violations []})

(def three-transactions-not-on-two-minutes-interval
  {:account {:availableLimit 50 :activeCard true}
             :transactions [{:merchant "starbucks" :amount 5 :time "2020-02-13T10:00:00.000Z"}
                            {:merchant "starbucks" :amount 1 :time "2020-02-13T10:05:00.000Z"}
                            {:merchant "starbucks" :amount 4 :time "2020-02-13T10:15:00.000Z"}]
             :violations []})

(def three-transactions-on-two-minutes-interval
  {:account {:availableLimit 50 :activeCard true}
             :transactions [{:merchant "starbucks" :amount 5 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 1 :time "2020-02-13T10:01:40.000Z"}
                            {:merchant "mac donalds" :amount 4 :time "2020-02-13T10:01:59.000Z"}]
             :violations []})

(def two-similar-transactions-on-two-minutes-interval
  {:account {:availableLimit 50 :activeCard true}
             :transactions [{:merchant "starbucks" :amount 1 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 1 :time "2020-02-13T10:01:40.000Z"}]
             :violations []})

(def two-not-similar-transactions-on-two-minutes-interval
  {:account {:availableLimit 50 :activeCard true}
             :transactions [{:merchant "starbucks" :amount 5 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 1 :time "2020-02-13T10:01:40.000Z"}]
             :violations []})

(def two-similar-transactions-not-on-two-minutes-interval
   {:account {:availableLimit 50 :activeCard true}
             :transactions [{:merchant "starbucks" :amount 5 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 5 :time "2020-02-13T10:41:40.000Z"}]
             :violations []})

(def multiple-violations-to-the-same-transaction
   {:account {:availableLimit 0 :activeCard false}
             :transactions []
             :violations []})

(def account-with-no-violations
  {:account {:availableLimit 30 :activeCard true}
             :transactions [{:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:40.000Z"}]
             :violations []})

(def account-with-insufficient-limit-violation
  {:account {:availableLimit 20 :activeCard true}
             :transactions [{:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:40.000Z"}]
             :violations [:insufficient-limit]})

(def account-with-multiple-violations
  {:account {:availableLimit 20 :activeCard false}
             :transactions [{:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:40.000Z"}]
             :violations [:card-not-active :insufficient-limit :high-frequency-small-interval]})

(def account-with-deny-list
  {:account {:availableLimit 20 :activeCard true :denyList ["merchant-a" "merchant-b"]}
   :transactions []
   :violations []})