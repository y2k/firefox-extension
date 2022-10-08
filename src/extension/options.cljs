(ns extension.options
  (:require [extension.domain :as d]
            [extension.storage :as s]
            [extension.effects :as eff]))

(defn document-loaded []
  (let [node (.-body js/document)]
    (doseq [cmd (d/make-options [:local (gensym)] (d/get-db) {:type :node :raw-node node})]
      (eff/execute-command cmd))))

(defonce setup
  (do
    (s/load-prefs #'document-loaded)
    nil))
