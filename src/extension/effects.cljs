(ns extension.effects
  (:require [extension.domain :as d]
            [extension.framework :as f]
            [clojure.edn :as edn]))

(defn init []
  (if (exists? js/browser)
    (->
     (js/browser.storage.local.get "key")
     (.then
      (fn [x]
        (let [p (some-> (.-key x) (.-value))
              db {:raw-config (if (some? p) p (str d/def-config))
                  :config (if (nil? p) d/def-config (edn/read-string p))}]
          (reset! d/db db)
          (f/dispatch :extension.domain/db-changed nil)))))
    (do
      (reset! d/db {:raw-config "{}"
                    :config {:exclude ["READ"]}})
      (f/dispatch :extension.domain/db-changed nil))))

(defn make-vnode [node]
  (defn inner-make-vnode [node]
    {:class (.-className node)
     :tagName (.-tagName node)
     :real-node node})
  (assoc (inner-make-vnode node) :parentNode (inner-make-vnode (.-parentNode node))))

(defn- document-append [{node :node n :target}]
  (defn get-real-node [node-desc]
    (if (string? node-desc)
      (.querySelector js/document node-desc)
      (:real-node n)))
  (let [h (.createElement js/document (:tagName node))]
    (set! (.-className h) (:class node))
    (set! (.-innerText h) (:innerText node))
    (set! (.-onclick h) (fn [e] (f/dispatch (:onclick node) {:target (make-vnode (.-target e))})))
    (.append (get-real-node n) h)))

(defn- document-click [p]
  (->
   (:real-node (:root p))
   (.querySelector (:querySelector p))
   (.click)))

(defn- document-remove [p] (.remove (:real-node p)))

(defonce setup
  (do
    (f/reg-fx :document/click #'document-click)
    (f/reg-fx :document/append #'document-append)
    (f/reg-fx :document/remove #'document-remove)
    (f/reg-fx :db (fn [new-db] (reset! d/db new-db)))

    (f/reg-event
     :extension.domain/db-reset-requested
     (fn [db]
       (reset! d/db db)
       (js/browser.storage.local.set (clj->js {:key {:value (:raw-config db)}}))
       (f/dispatch :extension.domain/db-changed nil)))
    nil))

;; Utils

(defn make-post-call []
  (let [last-timeout-id (atom 0)]
    (fn [f]
      (js/clearTimeout @last-timeout-id)
      (reset!
       last-timeout-id
       (js/setTimeout f 0)))))
