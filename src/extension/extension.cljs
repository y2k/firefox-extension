(ns extension.extension
  (:require [extension.domain :as d]
            [clojure.string :as str]
            [extension.effects :as eff]))

(defn hide-node [node]
  (.click (.querySelector node "img.extButton.threadHideButton")))

(defn query-model [model node]
  (into {} (map (fn [[k v]] [k (.-innerText (.querySelector node v))]) model)))

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

(do
  (d/reg-event-fx :extension.domain/db-changed (fn [_] (reload)))
  (eff/init)

  (let [target (.querySelector js/document "div.board")
        config #js {"childList" true}
        observer (js/MutationObserver.
                  (fn [mut-list _]
                    (doseq [m mut-list]
                      (if (= "childList" (.-type m))
                        (reload)))))]
    (.observe observer target config)))
