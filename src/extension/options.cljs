(ns extension.options
  (:require [extension.domain :as d]
            [clojure.edn :as edn]))

(defn- update-text-from-db []
  (->>
   (d/get-db)
   (:exclude)
   (str)
   (set! (.-value (.getElementById js/document "options-text")))))

(defn document-loaded []
  (update-text-from-db))

(defn- try-parse-db [input-str]
  (let [cfg (edn/read-string input-str)]
    (d/set-db {:exclude cfg})
    (update-text-from-db)))

(defn save-config []
  (let [input-str (.-value (.getElementById js/document "options-text"))
        cfg (edn/read-string input-str)]
    (d/set-db {:exclude cfg})
    (update-text-from-db)))

(defn cancel-config []
  (d/set-db (d/default-db))
  (update-text-from-db))

(defonce setup
  (do
    (set! (.-onclick (.querySelector js/document "#confirm")) #(save-config))
    (set! (.-onclick (.querySelector js/document "#cancel")) #(cancel-config))
    (document-loaded)
    nil))
