(ns extension.options
  (:require [extension.domain :as d]
            [extension.effects :as eff]))

(defonce setup
  (do
    (let [node (.getElementById js/document "root")]
      (doseq [cmd (d/make-options [:local (gensym)] (d/get-db) {:type :node :raw-node node})]
        (eff/execute-command cmd)))
    nil))
