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

(defn handle-media-changes []
  (doseq [node (.querySelectorAll js/document "video.expandedWebm:not(.ext-marked)")]
    (.add (.-classList node) "ext-marked")
    (let [parent-node (.-parentNode node)]
      (.append
       parent-node
       (let [close-btn (.createElement js/document "div")]
         (set! (.-className close-btn) "ext-hover")
         (set! (.-onclick close-btn)
               (fn []
                 (.remove close-btn)
                 (.click (.querySelector parent-node ".collapseWebm > a"))))
         close-btn)))))

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

(defn- add-user-menu []
  (->
   (.querySelector js/document "#navtopright")
   (.append
    (let [menu (.createElement js/document "a")]
      (set! (.-innerText menu) "[FADE]")
      (set! (.-onclick menu) (fn [] (js/alert "[FADE] clicked")))
      menu))))

(defonce db (atom {:config {:exclude []}}))

(defn document-changed []
  (handle-media-changes)
  (on-document-changed @db))

(defn document-loaded []
  (add-user-menu)
  (document-changed))

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

    (let [style (.createElement js/document "style")]
      (set!
       (.-innerHTML style)
       "video.expandedWebm { grid-row: 2; grid-column: 1; }
        div.ext-hover { margin: 5px 0px 45px 0px; grid-row: 2; grid-column: 1; }
        div.post div.file { display: grid }
        a.fileThumb img { opacity: 0.05 }")
      (.append (.-head js/document) style))

    (document-loaded)

    nil))
