(ns authorizer.core
  (:require [authorizer.logic :as logic]
            [authorizer.utils :as utils]))

(defn- exec
  [func state input-line]
  (let [updated-state (func state input-line)]
    (println (-> (logic/get-account-response updated-state)
                 (utils/map->json)))
    (dissoc updated-state :violations)))

(defn- exec-input!
  [state]
  (let [input-line (utils/json->map! (read-line))]
    (cond
      (logic/is-account? input-line)
        (-> (exec logic/new-account state input-line)
            (recur))

      (logic/is-transaction? input-line)
        (-> (exec logic/execute-transaction state input-line)
            (recur)))))

(defn -main
  []
  (exec-input! {}))