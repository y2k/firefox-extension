(ns extension.extension
  (:require [extension.domain :as d]
            [clojure.string :as str]
            [extension.effects :as eff]))

(defn hide-node [node]
  (.click (.querySelector node "img.extButton.threadHideButton")))

(defn reload []
  (->>
   (.querySelectorAll js/document "div.thread:not(.post-hidden)")
   (map
    (fn [node]
      {:title (.-innerText (.querySelector node "span.subject"))
       :body (.-innerText (.querySelector node "blockquote.postMessage"))
       :name (.-innerText (.querySelector node "span.name"))
       :node node}))
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
