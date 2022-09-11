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

(defn clear-content-on-changes [db v-node]

  (if (= "thread" (:class v-node))

    (d/skip-nodes
     (:exclude (:config db))
     []))

;; (->>
;;  (.querySelectorAll js/document "div.thread:not(.post-hidden)")
;;  (map
;;   (fn [node]
;;     (->
;;      (query-model
      ;; {:title "span.subject"
      ;;  :body "blockquote.postMessage"
      ;;  :name "span.name"}
;;       node)
;;      (assoc :node node))))
;;  (d/skip-nodes (:exclude (:config db)))
;;  (run! (fn [x] (some-> (.querySelector (:node x) "img.extButton.threadHideButton") (.click)))))
  )

(comment

  (defn skip-nodes [exclude nodes]
    (filter
     (fn [{title :title body :body :as node}]
       (let [sample (first (remove str/blank? [title body]))]
         (apply contains-strings sample exclude)))
     nodes))

  (ns inject-co-effects
    (defn select-nodes [root-selector child-selector]
      [:select-nodes
       {:selector root-selector
        :target child-selector}]))

  {:rules
   [{:input
     [[:db nil]
      [:select-nodes
       {:selector "div.thread:not(.post-hidden)"
        :target {:title "span.subject"
                 :body "blockquote.postMessage"
                 :hide-btn "img.extButton.threadHideButton"}}]]
     :handler
     (fn [db nodes]
       (let [exclude (:exclude (:config db))]
         (->>
          nodes
          (filter
           (fn [node]
             (not
              (or
               (apply d/contains-strings (:innerText (:title node)) exclude)
               (apply d/contains-strings (:innerText (:body node)) exclude)))))
          (map
           (fn [node]
             [:document/click {:root (:hide-btn node)}])))))}]}

  comment-end)

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

(defn- add-user-menu []
  [:document/append
   {:target "#navtopright"
    :node {:tagName "A"
           :innerText "[FADE]"
           :onclick ::fade-clicked}}])

(defn- fade-clicked [db]
  [:db (assoc db :fade-content (not (:fade-content db)))])

;; ==============================

;; (comment

;;   (defn clear-content-on-start [n]
;;     (if (= (:className n) "thread")
;;       (let
;;        [x {:class (str "'" (.-className n) "'")
;;            :title (.-innerText (.querySelector n "span.subject"))
;;            :body  (.-innerText (.querySelector n "blockquote.postMessage"))
;;            :name  (.-innerText (.querySelector n "span.name"))}]
;;         (println x))))

;;   comment)

;; ==============================

(comment

  (def ^:private cofx-handlers (atom {}))

  (defn reg-cofx [name f]
    ___)

  (reg-event-fx
   ::document-loaded
   [(eff/select-nodes
     "div.thread:not(.post-hidden)"
     {:title "span.subject"
      :body "blockquote.postMessage"
      :hide-btn "img.extButton.threadHideButton"})]
   (fn [{nodes :select-nodes db :db}]
     (let [exclude (:exclude (:config db))]
       (->>
        nodes
        (filter
         (fn [{title :title body :body}]
           (let [sample (first (remove str/blank? [(:innerText title) (:innerText body)]))]
             (apply d/contains-strings sample exclude))))
        (map
         (fn [{node :hide-btn}]
           [:document/click {:root node}]))))))

  comment-end)

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

(comment

  (def stop
    (let [observer
          (js/MutationObserver.
           (fn [mutations _]
             (doseq [m mutations]
               (doseq [n (.-addedNodes m)]
              ;;  (println "call timeout-call")
                 (timeout-call)))))]
      (.observe
       observer
       (.querySelector js/document "div.board")
       #js{"subtree" true "childList" true})
      (fn [] (.disconnect observer))))

  (defn mk-timeout-call []
    (let [last-timeout-id (atom 0)]
      (fn []
        (js/clearTimeout @last-timeout-id)
        (reset!
         last-timeout-id
         (js/setTimeout
          (fn [] (println "hello"))
          0)))))

  (def timeout-call
    (let [last-timeout-id (atom 0)]
      (fn []
        (js/clearTimeout @last-timeout-id)
        (reset!
         last-timeout-id
         (js/setTimeout
          (fn [] (println "hello"))
          0)))))

  comment)

(defonce setup
  (do
    (f/reg-event ::document-loaded #'add-user-menu)
    (f/reg-event-db ::fade-clicked #'fade-clicked)

    ;; (f/reg-event :document-node-added #'handle-media-changes)
    ;; (f/reg-event ::close-clicked #'close-clicked)

    ;; (f/reg-event-db :document-node-added #'clear-content-on-changes)

    ;; (let [observer
    ;;       (js/MutationObserver.
    ;;        (fn [mutations _]
    ;;          (doseq [m mutations]
    ;;            (doseq [n (.-addedNodes m)]
    ;;              (f/dispatch :document-node-added (eff/make-vnode n))))))]
    ;;   (.observe
    ;;    observer
    ;;    (.querySelector js/document "div.board")
    ;;    #js{"subtree" true "childList" true})
    ;;   (fn [] (.disconnect observer)))

    (def stop
      (let [post-call (eff/make-post-call)
            observer (js/MutationObserver.
                      (fn [mutations _]
                        (doseq [m mutations]
                          (doseq [n (.-addedNodes m)]
                            (post-call (fn [] (f/dispatch ::document-changed nil)))))))]
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

    (f/dispatch ::document-loaded nil)

    nil))
