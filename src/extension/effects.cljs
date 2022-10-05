(ns extension.effects
  (:require [clojure.string :as str]
            [extension.domain :as d]
            [extension.storage :as s]))

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

(declare execute-command)

(defn- add-node [target vnode]
  (.append
   target
   (let [node (.createElement js/document (:tag vnode))]
     (some->> (:class vnode) (set! (.-className node)))
     (some->> (:innerText vnode) (set! (.-innerText node)))
     (some->> (:value vnode) (set! (.-value node)))
     (if-let [f (:onchange vnode)]
       (set! (.-onchange node)
             (fn [e]
               (doseq [cmd (f (d/get-db) {:target {:type :node :value (.-value (.-target e)) :raw-node (.-target e)}})]
                 (execute-command cmd)))))
     (if-let [f (:onclick vnode)]
       (set! (.-onclick node)
             (fn [e]
               (doseq [cmd (f (d/get-db) {:target {:type :node :raw-node (.-target e)}})]
                 (execute-command cmd)))))
     (doseq [child (:children vnode)]
       (add-node node child))
     node)))

(defn execute-command [cmd]
  (d/trace "execute-command" cmd)
  (let [[cmd-name cmd-arg] cmd]
    (case cmd-name
      :io (cmd-arg)
      :click (some-> (get-real-node cmd-arg) (.click))

      :set-value (set! (.-value (get-real-node (:target cmd-arg))) (:value cmd-arg))
      :add-class (.add (.-classList (get-real-node (:target cmd-arg))) (:class cmd-arg))

      :remove-node (.remove (get-real-node cmd-arg))
      :add-element (add-node (get-real-node (:target cmd-arg)) cmd-arg)

      :db (do
            (d/set-db cmd-arg)
            (s/save-prefs)
            (sync-css-variables)))))
