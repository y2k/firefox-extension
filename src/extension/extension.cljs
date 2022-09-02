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

(comment

  (type 0)
  (js/alert "test")

  (defn handle-page-changed [n]
    (if (= (.-className n) "thread")
      (let
       [x {:class (str "'" (.-className n) "'")
           :title (.-innerText (.querySelector n "span.subject"))
           :body  (.-innerText (.querySelector n "blockquote.postMessage"))
           :name  (.-innerText (.querySelector n "span.name"))}]
        (println x))))

  (def
    disconnect
    (let [observer
          (js/MutationObserver.
           (fn [mutations _]
             (doseq [m mutations]
               (doseq [n (.-addedNodes m)]
                 (handle-page-changed n)))))]
      (.observe
       observer
       (.querySelector js/document "div.board")
       #js{"subtree" true "childList" true})
      (fn [] (.disconnect observer))))

  (defn handle-page-changed []
    ???)

  (defn document-node-added-virt [v-node]
    (if (= "VIDEO" (:tagName v-node))
      (let [parent-node (:parentNode v-node)]
        [[:document/append
          {:target parent-node
           :node {:tagName "DIV"
                  :class "ext-hover"
                  :onclick ::close-clicked}}]])))

  (defn close-clicked [{target :target}]
    (let [parent-node (:parentNode target)]
      [[:document/remove target]
       [:document/click {:root parent-node :querySelector ".collapseWebm > a"}]]))

  (close-clicked {:target {:id "node1" :parentNode {:id "node2"}}})

  (reg-event ::close-clicked close-clicked)

  (defn- make-vnode [node]
    (defn inner-make-node [node]
      {:class (.-className node)
       :tagName (.-tagName node)
       :real-node node})
    (assoc (inner-make-node node) :parent (inner-make-vnode (.-parentNode node))))

  (reg-fx
   :document/append
   (fn [{node :node n :target}]
     (let [h (.createElement js/document (:tagName node))]
       (set! (.-className h) (:class node))
       (set! (.-onclick h) (fn [e] (f/dispatch2 (:onclick node) {:target (make-vnode (.-target e))})))
       (.append (:real-node n) h))))

  (reg-fx
   :document/click
   (fn [p]
     (->
      (:real-node (:root p))
      (.querySelector (:querySelector p))
      (.click))))

  (reg-fx :document/remove (fn [p] (.click (:real-node p))))

  (let [node {:tagName "VIDEO" :real-node (gensym) :parentNode {:tagName "PARENT" :real-node (gensym)}}]
    (->
     (document-node-added-virt node)
     (first)
     (second)
     (:node)
     (:onclick)))

  comment)

(defn document-node-added [node]
  (if (= "VIDEO" (.-tagName node))
    (let [n (.-parentNode node)]
      (let [h (.createElement js/document "div")]
        (set! (.-className h) "ext-hover")
        (set! (.-onclick h)
              (fn [e]
                (-> (.querySelector n ".collapseWebm > a") (.click))
                (.remove (.-target e))))
        (.append n h)))))

;; (defonce setup
;;   (do
;;     (f/reg-event-db :document-node-added handle-page-changed)
;;     (f/reg-event-db :extension.domain/db-changed handle-page-changed)
;;     (f/reg-event :document-node-added document-node-added)

;;     (let [style (.createElement js/document "style")]
;;       (set!
;;        (.-innerHTML style)
;;        "video.expandedWebm { grid-row: 2; grid-column: 1; }
;;         div.ext-hover { margin: 5px 0px 45px 0px; grid-row: 2; grid-column: 1; }
;;         div.post div.file { display: grid }")
;;       (.append (.-head js/document) style))

;;     (.observe
;;      (js/MutationObserver.
;;       (fn [mutations _]
;;         (doseq [m mutations]
;;           (doseq [n (.-addedNodes m)]
;;             (f/dispatch [:document-node-added n])))))
;;      (.querySelector js/document "div.board")
;;      #js{"subtree" true "childList" true})

;;     (eff/init)

;;     nil))
