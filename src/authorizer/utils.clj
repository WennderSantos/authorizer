(ns authorizer.utils
  (:require [clojure.data.json :as json]))

(defn json->map!
  [json]
  (try
    (json/read-str json :key-fn keyword)
  (catch Exception _ nil)))

(defn map->json
  [map]
  (json/write-str map))