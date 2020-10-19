(ns authorizer.state_fixture)

(def account-with-1mm-available-limit
  {:account {:available-limit 1000000 :active-card true}
   :transactions []
   :violations []})

(def account-with-1-available-limit
  {:account {:available-limit 1 :active-card true}
   :transactions []
   :violations []})

(def account-with-active-card
  {:account {:available-limit 20 :active-card true}
   :transactions []
   :violations []})

(def account-with-inactive-card
  {:account {:available-limit 20 :active-card false}
   :transactions []
   :violations []})

(def three-transactions-not-on-two-minutes-interval
  {:account {:available-limit 50 :active-card true}
             :transactions [{:merchant "starbucks" :amount 5 :time "2020-02-13T10:00:00.000Z"}
                            {:merchant "starbucks" :amount 1 :time "2020-02-13T10:05:00.000Z"}
                            {:merchant "starbucks" :amount 4 :time "2020-02-13T10:15:00.000Z"}]
             :violations []})

(def three-transactions-on-two-minutes-interval
  {:account {:available-limit 50 :active-card true}
             :transactions [{:merchant "starbucks" :amount 5 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 1 :time "2020-02-13T10:01:40.000Z"}
                            {:merchant "mac donalds" :amount 4 :time "2020-02-13T10:01:59.000Z"}]
             :violations []})

(def two-similar-transactions-on-two-minutes-interval
  {:account {:available-limit 50 :active-card true}
             :transactions [{:merchant "starbucks" :amount 1 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 1 :time "2020-02-13T10:01:40.000Z"}]
             :violations []})

(def two-not-similar-transactions-on-two-minutes-interval
  {:account {:available-limit 50 :active-card true}
             :transactions [{:merchant "starbucks" :amount 5 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 1 :time "2020-02-13T10:01:40.000Z"}]
             :violations []})

(def two-similar-transactions-not-on-two-minutes-interval
   {:account {:available-limit 50 :active-card true}
             :transactions [{:merchant "starbucks" :amount 5 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 5 :time "2020-02-13T10:41:40.000Z"}]
             :violations []})

(def multiple-violations-to-the-same-transaction
   {:account {:available-limit 10 :active-card true}
             :transactions [{:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:40.000Z"}]
             :violations []})

(def account-with-no-violations
  {:account {:available-limit 30 :active-card true}
             :transactions [{:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:40.000Z"}]
             :violations []})

(def account-with-insufficient-limit-violation
  {:account {:available-limit 20 :active-card true}
             :transactions [{:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:40.000Z"}]
             :violations [:insufficient-limit]})

(def account-with-multiple-violations
  {:account {:available-limit 20 :active-card false}
             :transactions [{:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:00.000Z"}
                            {:merchant "starbucks" :amount 10 :time "2020-02-13T10:01:40.000Z"}]
             :violations [:card-not-active :insufficient-limit :high-frequency-small-interval]})