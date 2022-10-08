(ns extension.extension
  (:require [clojure.string :as str]
            [extension.domain :as d]
            [extension.storage :as s]
            [extension.effects :as eff]))

;; Framework

(defn- query-model [model node]
  (->>
   model
   (map (fn [[k v]] [k {:type :node
                        :raw-node (.querySelector node v)
                        :innerText (.-innerText (.querySelector node v))}]))
   (into {})))

(defn- on-document-changed [fbegin fend]
  (->>
   (.querySelectorAll js/document (get (fbegin) 0))
   (map
    (fn [node]
      (->
       (query-model (get (fbegin) 1) node)
       (assoc :node {:type :node :raw-node node}))))
   (fend (d/get-db))
   (run! eff/execute-command)))

(defn- document-changed []
  (on-document-changed d/handle-media-changes-begin d/handle-media-changes-end)
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
        a.fileThumb img { opacity: var(--ext_content_opacity) }
        #navtopright { display: flex }")
      (.append (.-head js/document) style))

    (s/load-prefs #'document-loaded)

    nil))
