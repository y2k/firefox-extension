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

(defn clear-content [db _]
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

;; ==============================

(defn- document-loaded []
  [:document/append
   {:target "#navtopright"
    :node {:tagName "A"
           :innerText "[FADE]"
           :onclick ::fade-clicked}}])

(defn- fade-clicked [db]
  [:db (assoc db :fade-content (not (:fade-content db)))])

(comment
  (defn handle-page-changed [n]
    (if (= (.-className n) "thread")
      (let
       [x {:class (str "'" (.-className n) "'")
           :title (.-innerText (.querySelector n "span.subject"))
           :body  (.-innerText (.querySelector n "blockquote.postMessage"))
           :name  (.-innerText (.querySelector n "span.name"))}]
        (println x))))

  comment)

;; ==============================

(defn handle-media-changes [v-node]
  (if (= "VIDEO" (:tagName v-node))
    (let [parent-node (:parentNode v-node)]
      [:document/append
       {:target parent-node
        :node {:tagName "DIV"
               :class "ext-hover"
               :onclick ::close-clicked}}])))

(defn close-clicked [{target :target}]
  (let [parent-node (:parentNode target)]
    [:batch
     [[:document/remove target]
      [:document/click {:root parent-node :querySelector ".collapseWebm > a"}]]]))

(defonce setup
  (do
    (f/reg-event ::document-loaded #'document-loaded)
    (f/reg-event-db ::fade-clicked #'fade-clicked)

    (f/reg-event :document-node-added #'handle-media-changes)
    (f/reg-event ::close-clicked #'close-clicked)

    (f/reg-event-db :document-node-added #'clear-content)
    (f/reg-event-db ::document-loaded #'clear-content)
    ;; (f/reg-event-db :extension.domain/db-changed handle-page-changed)

    (let [observer
          (js/MutationObserver.
           (fn [mutations _]
             (doseq [m mutations]
               (doseq [n (.-addedNodes m)]
                 (f/dispatch :document-node-added (eff/make-vnode n))))))]
      (.observe
       observer
       (.querySelector js/document "div.board")
       #js{"subtree" true "childList" true})
      (fn [] (.disconnect observer)))

    (let [style (.createElement js/document "style")]
      (set!
       (.-innerHTML style)
       "video.expandedWebm { grid-row: 2; grid-column: 1; }
        div.ext-hover { margin: 5px 0px 45px 0px; grid-row: 2; grid-column: 1; }
        div.post div.file { display: grid }")
      (.append (.-head js/document) style))

    ;; (eff/init)
    (f/dispatch ::document-loaded nil)

    nil))
