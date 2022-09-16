(ns extension.options
  (:require [extension.domain :as d]
            [extension.storage :as s]
            [clojure.edn :as edn]))

(defn- update-text-from-db []
  (->>
   (d/get-db)
   (:exclude)
   (str)
   (set! (.-value (.getElementById js/document "options-text")))))

(defn- document-loaded []
  (update-text-from-db))

(defn- save-config []
  (let [input-str (.-value (.getElementById js/document "options-text"))
        cfg (edn/read-string input-str)]
    (d/set-db {:exclude cfg})
    (s/save-prefs)
    (update-text-from-db)))

(defn- cancel-config []
  (update-text-from-db))

(defonce setup
  (do
    (s/load-prefs #'document-loaded)
    (set! (.-onclick (.querySelector js/document "#confirm")) #(save-config))
    (set! (.-onclick (.querySelector js/document "#cancel")) #(cancel-config))
    (document-loaded)
    nil))
