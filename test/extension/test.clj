(ns extension.test
  (:require [clojure.test :refer :all]))

(deftest skip-nodes
  (is
    (do
      (println "Add test!!!")
      true)))

(comment
  (ns extension.test)
  (run-tests)
  (comment))
