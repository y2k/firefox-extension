(ns extension.storage
  (:require [clojure.string :as str]
            [extension.domain :as d]
            [clojure.edn :as edn]))

(defn load-prefs [callback]
  (if (exists? js/browser)
    (->
     (js/browser.storage.local.get "key")
     (.then
      (fn [x]
        (some->
         (.-key x)
         (.-value)
         (edn/read-string)
         (d/set-db))
        (callback))))))

(defn save-prefs []
  (if (exists? js/browser)
    (js/browser.storage.local.set
     (clj->js {:key {:value (str (d/get-db))}}))))
