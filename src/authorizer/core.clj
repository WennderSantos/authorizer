(ns authorizer.core
  (:require [authorizer.rules :as rules]
            [authorizer.utils :as utils]))

(defn- exec
  [func state input-line]
  (let [updated-state (func state input-line)]
    (println (-> (rules/get-account-response updated-state)
                 (utils/map->json)))
    (dissoc updated-state :violations)))

(defn- exec-input!
  [state]
  (let [input-line (utils/json->map! (read-line))]
    (cond
      (rules/is-account? input-line)
        (-> (exec rules/new-account state input-line)
            (recur))

      (rules/is-transaction? input-line)
        (-> (exec rules/execute-transaction state input-line)
            (recur)))))

(defn -main
  []
  (exec-input! {}))