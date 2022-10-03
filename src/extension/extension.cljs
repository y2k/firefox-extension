(ns extension.extension
  (:require [clojure.string :as str]
            [extension.domain :as d]
            [extension.storage :as s]))

;; Framework

(defn- sync-css-variables []
  (let [r (.querySelector js/document ":root")
        vars (:css-variables (d/get-db))]
    (doseq [[k v] vars]
      (.setProperty r.style k v))))

(defn- get-real-node [vnode]
  (case (:type vnode)
    :node (:raw-node vnode)
    :parent (.-parentNode (get-real-node (:target vnode)))
    :selector (.querySelector (get-real-node (:target vnode)) (:selector vnode))))

(defn- execute-command [cmd]
  (let [[cmd-name cmd-arg] cmd]
    (d/trace "[execute-command]" (str "name: " cmd-name ", arg: " cmd-arg))
    (case cmd-name
      :click (some->
              (get-real-node cmd-arg)
              (.click))

      :add-class (.add (.-classList (get-real-node (:target cmd-arg))) (:class cmd-arg))

      :remove-node (.remove (get-real-node cmd-arg))

      :add-element (->
                    (get-real-node (:target cmd-arg))
                    (.append
                     (let [menu (.createElement js/document (:tag cmd-arg))]
                       (set! (.-className menu) (:class cmd-arg))
                       (set! (.-innerText menu) (:innerText cmd-arg))
                       (set! (.-onclick menu)
                             (fn [e]
                               (doseq [cmd ((:onclick cmd-arg) (d/get-db) {:target {:type :node :raw-node (.-target e)}})]
                                 (execute-command cmd))))
                       menu)))

      :db (do
            (d/set-db cmd-arg)
            (s/save-prefs)
            (sync-css-variables))

      :update-db (do
                   (d/update-db (fn [db] (cmd-arg db)))
                   (s/save-prefs)
                   (sync-css-variables)))))

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
   (run! execute-command)))

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
        a.fileThumb img { opacity: var(--ext_content_opacity) }")
      (.append (.-head js/document) style))

    (s/load-prefs #'document-loaded)

    nil))
