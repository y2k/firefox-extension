(ns extension.extension
  (:require [clojure.string :as str]))

;; Domain

(defn- contains-strings [s & subs]
  (let [low-s (str/lower-case s)
        low-subs (mapv (fn [x] (str/lower-case x)) subs)]
    (boolean (some (fn [x] (str/includes? low-s x)) low-subs))))

(defn skip-nodes [exclude nodes]
  (filter
   (fn [{title :title body :body :as node}]
     (let [sample (first (remove str/blank? [(:innerText title) (:innerText body)]))]
       (apply contains-strings sample exclude)))
   nodes))

;; Framework

(defn query-model [model node]
  (->>
   model
   (map (fn [[k v]] [k {:innerText (.-innerText (.querySelector node v))}]))
   (into {})))

(defn on-document-changed [db]
  (->>
   (.querySelectorAll js/document "div.thread:not(.post-hidden)")
   (map
    (fn [node]
      (->
       (query-model
        {:title "span.subject"
         :body "blockquote.postMessage"
         :name "span.name"}
        node)
       (assoc :node node))))
   (skip-nodes (:exclude (:config db)))
   (run! (fn [x] (some-> (.querySelector (:node x) "img.extButton.threadHideButton") (.click))))))

(defonce db (atom {:config {:exclude []}}))

(defn document-changed []
  (on-document-changed @db))

;; Main

(defonce setup
  (do

    (def stop
      (let [post-call (let [last-timeout-id (atom 0)]
                        (fn []
                          (js/clearTimeout @last-timeout-id)
                          (reset!
                           last-timeout-id
                           (js/setTimeout document-changed 0))))
            observer (js/MutationObserver.
                      (fn [mutations _]
                        (doseq [m mutations]
                          (doseq [n (.-addedNodes m)]
                            (post-call)))))]
        (.observe
         observer
         (.querySelector js/document "div.board")
         #js{"subtree" true "childList" true})
        (fn [] (.disconnect observer))))

    (document-changed)

    nil))
