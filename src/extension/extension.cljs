(ns extension.extension
  (:require [extension.domain :as d]
            [extension.framework :as f]
            [clojure.string :as str]
            [extension.effects :as eff]))

(defn hide-node [node]
  (.click (.querySelector node "img.extButton.threadHideButton")))

(defn query-model [model node]
  (->>
   model
   (map (fn [[k v]] [k (.-innerText (.querySelector node v))]))
   (into {})))

(defn reload []
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
   (d/skip-nodes (:exclude (:config @d/db)))
   (run! (fn [x] (hide-node (:node x))))))

(defonce setup
  (do
    (f/reg-event-fx :extension.domain/db-changed (fn [_] (reload)))
    (eff/init)

    (.observe
     (js/MutationObserver.
      (fn [mut-list _]
        (doseq [m mut-list]
          (if (= "childList" (.-type m))
            (reload)))))
     (.querySelector js/document "div.board")
     #js{"childList" true})
    nil))
