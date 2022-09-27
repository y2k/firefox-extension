(ns extension.extension
  (:require [clojure.string :as str]
            [extension.domain :as d]
            [extension.storage :as s]))

;; Framework

(defn- handle-media-changes []
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

(defn- query-model [model node]
  (->>
   model
   (map (fn [[k v]] [k {:raw-node (.querySelector node v)
                        :innerText (.-innerText (.querySelector node v))}]))
   (into {})))

(defn- execute-command [cmd]
  (fn [cmd]
    (cond
      (= :click (get cmd 0))
      (some->
       (:raw-node (get cmd 1))
       (.click))

      (= :add-element (get cmd 0))
      (->
       (:raw-node (get cmd 1))
       (.append
        (let [menu (.createElement js/document (:tag (get cmd 1)))]
          (set! (.-innerText menu) (:innerText (get cmd 1)))
          (set! (.-onclick menu)
                (fn []
                  (doseq [cmd ((:onclick (get cmd 1)))]
                    (execute-command cmd))))
          menu)))

      (= :update-db (get cmd 0))
      (do
        (d/update-db (fn [db] ((get cmd 1) db)))
        (s/save-prefs)))))

(defn- on-document-changed [fbegin fend]
  (->>
   (.querySelectorAll js/document (get (fbegin) 0))
   (map
    (fn [node]
      (->
       (query-model (get (fbegin) 1) node)
       (assoc :node node))))
   (fend (d/get-db))
   (run! execute-command)))

(defn- document-changed []
  (handle-media-changes)
  (on-document-changed d/on-document-changed-begin d/on-document-changed-end))

(defn- document-loaded []
  (on-document-changed d/add-user-menu-begin d/add-user-menu-end)
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

    (s/load-prefs #'document-loaded)

    nil))
