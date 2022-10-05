(ns extension.storage
  (:require [clojure.string :as str]
            [extension.domain :as d]
            [clojure.edn :as edn]))

(defn add-local-state [db]
  (assoc db :local (:local (d/get-db))))

(defn load-prefs [callback]
  (if (exists? js/browser.storage)
    (->
     (js/browser.storage.local.get "key")
     (.then
      (fn [x]
        (some->
         (.-key x)
         (.-value)
         (edn/read-string)
         (add-local-state)
         (d/set-db))
        (callback))))
    (callback)))

(defn save-prefs []
  (if (exists? js/browser.storage)
    (js/browser.storage.local.set
     (clj->js {:key {:value (str (dissoc (d/get-db) :local))}}))))
