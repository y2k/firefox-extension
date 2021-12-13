(ns extension.test
  (:require [clojure.test :refer :all]
            [extension.domain :as d]))

(deftest skip-nodes
  (is (empty?
       (d/skip-nodes
        ["foo"]
        [{:title "title" :body "body"}]))))

(comment
  (ns extension.test)
  (run-tests)
  (comment))
