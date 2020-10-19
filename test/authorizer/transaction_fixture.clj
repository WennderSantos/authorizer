(ns authorizer.transaction_fixture
  (:require [authorizer.utils :as utils]))

(def three-on-two-minutes-interval
  [{:merchant "Burger King" :amount 10 :time "2019-02-13T10:00:00.000Z"}
   {:merchant "Burger King" :amount 11 :time "2019-02-13T10:00:00.000Z"}
   {:merchant "Burger King" :amount 10 :time "2019-02-13T10:00:00.000Z"}])

(def two-on-two-minutes-interval
  [{:merchant "Burger King" :amount 10 :time "2019-02-13T10:00:00.000Z"}
   {:merchant "Burger King" :amount 11 :time "2019-02-13T10:00:00.000Z"}])

(def five-not-on-two-minutes-interval
  [{:merchant "mc donalds" :amount 42 :time "2019-02-12T10:01:50.000Z"}
   {:merchant "Burger King" :amount 110 :time "2019-02-13T10:01:59.000Z"}
   {:merchant "mc donalds" :amount 28 :time "2019-02-13T11:22:05.000Z"}
   {:merchant "Burger King" :amount 10 :time "2019-02-13T11:23:58.000Z"}
   {:merchant "mc donalds" :amount 12 :time "2019-03-19T11:05:05.000Z"}])

(def two-similar-in-sequence-on-two-minutes-interval
  [{:merchant "Burger King" :amount 10 :time "2019-02-13T10:00:00.000Z"}
   {:merchant "Burger King" :amount 10 :time "2019-02-13T10:00:00.000Z"}])

(def two-similar-not-in-sequence-on-two-minutes-interval
  [{:merchant "mc donalds" :amount 12 :time "2019-02-13T10:01:50.000Z"}
   {:merchant "Burger King" :amount 10 :time "2019-02-13T10:01:59.000Z"}
   {:merchant "starbucks" :amount 55 :time "2019-02-13T10:02:05.000Z"}
   {:merchant "Burger King" :amount 10 :time "2019-02-13T10:02:08.000Z"}])

(def two-similar-not-on-two-minutes-interval
  [{:merchant "mc donalds" :amount 12 :time "2019-02-13T10:01:50.000Z"}
   {:merchant "Burger King" :amount 10 :time "2019-02-13T10:01:59.000Z"}
   {:merchant "mc donalds" :amount 12 :time "2019-02-13T11:02:05.000Z"}
   {:merchant "Burger King" :amount 10 :time "2019-02-13T11:02:08.000Z"}
   {:merchant "mc donalds" :amount 12 :time "2019-02-13T11:05:05.000Z"}])

(def three-not-similar
  [{:merchant "Burger King" :amount 10 :time "2019-02-13T10:00:00.000Z"}
   {:merchant "mc donalds" :amount 11 :time "2019-02-13T10:00:00.000Z"}])

(def one-regular
   {:merchant "Burger King" :amount 10 :time "2019-02-13T10:00:00.000Z"})

(def with-10-amount
   {:merchant "starbucks" :amount 10 :time "2020-02-13T10:00:00.000Z"})
