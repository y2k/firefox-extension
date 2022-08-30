(ns extension.extension
  (:require [extension.domain :as d]
            [extension.framework :as f]
            [clojure.string :as str]
            [extension.effects :as eff]))

(defn query-model [model node]
  (->>
   model
   (map (fn [[k v]] [k (.-innerText (.querySelector node v))]))
   (into {})))

(defn handle-page-changed [db _]
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
   (d/skip-nodes (:exclude (:config db)))
   (run! (fn [x] (some-> (.querySelector (:node x) "img.extButton.threadHideButton") (.click))))))

(f/reg-event-fx
 :document-node-added
 (fn [node]
   (if (= "VIDEO" (.-tagName node))
     (let [n (.-parentNode node)]
       (let [h (.createElement js/document "div")]
         (set! (.-className h) "ext-hover")
         (set! (.-onclick h)
               (fn [e]
                 (-> (.querySelector n ".collapseWebm > a") (.click))
                 (.remove (.-target e))))
         (.append n h))))))

(defonce setup
  (do
    (f/reg-event-db :document-node-added handle-page-changed)
    (f/reg-event-db :extension.domain/db-changed handle-page-changed)

    (let [style (.createElement js/document "style")]
      (set!
       (.-innerHTML style) "
          video.expandedWebm { grid-row: 2; grid-column: 1; }
          div.ext-hover { margin: 5px 0px 45px 0px; grid-row: 2; grid-column: 1; }
          div.post div.file { display: grid }
          ")
      (.append (.-head js/document) style))

    (.observe
     (js/MutationObserver.
      (fn [mutations _]
        (doseq [m mutations]
          (doseq [n (.-addedNodes m)]
            (f/dispatch [:document-node-added n])))))
     (.querySelector js/document "div.board")
     #js{"subtree" true "childList" true})

    (eff/init)

    nil))
