(ns extension.domain
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))

;; =========================================================

(defn trace [prefix n]
  (println "[LOG]" prefix n)
  n)

(defn trace_ [n posfix]
  (println "[LOG]" n "|" posfix)
  n)

;; =========================================================

(defn default-db [] {:exclude []})

(defonce ^:private db (atom (default-db)))

(defn get-db [] @db)
(defn set-db [new-db] (reset! db new-db))

(defn update-db [f]
  (swap! db f))

;; =========================================================

(defn- contains-strings [s & subs]
  (let [low-s (str/lower-case s)
        low-subs (mapv (fn [x] (str/lower-case x)) subs)]
    (boolean (some (fn [x] (str/includes? low-s x)) low-subs))))

(defn- skip-nodes [exclude nodes]
  (filter
   (fn [{title :title body :body :as node}]
     (let [sample (first (remove str/blank? [(:innerText title) (:innerText body)]))]
       (apply contains-strings sample exclude)))
   nodes))

;; =========================================================

(defn on-document-changed-begin []
  ["div.thread:not(.post-hidden)"
   {:title "span.subject"
    :body "blockquote.postMessage"
    :button "img.extButton.threadHideButton"}])

(defn on-document-changed-end [db entities]
  (->>
   entities
   (skip-nodes (:exclude db))
   (mapcat
    (fn [entity]
      [[:click (:button entity)]]))))

;; =========================================================

(defn add-user-menu-begin []
  ["#navtopright" {}])

(defn add-user-menu-end [_ entities]
  [[:add-element
    {:target (:node (nth entities 0))
     :tag "A"
     :innerText "[FADE]"
     :onclick
     (fn []
       [[:update-db (fn [db] (update db :toggle-visible not))]])}]])

;; =========================================================

(defn handle-media-changes-begin []
  ["video.expandedWebm:not(.ext-marked)"
   {}])

(defn handle-media-changes-end [_ entities]
  (mapcat
   (fn [entity]
     (let [parent-node {:type :parent
                        :target (:node entity)}]
       [[:add-class
         {:target (:node entity)
          :class "ext-marked"}]
        [:add-element
         {:target parent-node
          :tag "DIV"
          :class "ext-hover"
          :onclick
          (fn [e]
            [[:remove-node (:target e)]
             [:click
              {:type :selector
               :target parent-node
               :selector ".collapseWebm > a"}]])}]]))
   entities))
