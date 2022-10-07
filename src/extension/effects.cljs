(ns extension.effects
  (:require [clojure.string :as str]
            [goog.object :as g]
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
   (let [node (.createElement js/document (name (nth vnode 0)))]
     (doseq [[k v] (nth vnode 1)]
       (if (fn? v)
         (g/set node (name k)
                (fn [e]
                  (doseq [cmd (v (d/get-db) {:target {:type :node :value (.-value (.-target e)) :raw-node (.-target e)}})]
                    (execute-command cmd))))
         (g/set node (name k) v)))
     (doseq [child  (subvec vnode 2)]
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
      :add-node (add-node (get-real-node (:target cmd-arg)) (:node cmd-arg))

      :db (do
            (d/set-db cmd-arg)
            (s/save-prefs)
            (sync-css-variables)))))
