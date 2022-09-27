(ns extension.domain
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))

(defn default-db [] {:exclude []})

(def ^:private db (atom (default-db)))

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
   (skip-nodes (:exclude (:config db)))
   (mapcat
    (fn [entity]
      [[:click (:button entity)]]))))

;; =========================================================

(defn add-user-menu-begin []
  ["#navtopright" {}])

(defn add-user-menu-end [_ entities]
  [[:add-element
    {:root (get entities 0)
     :tag "A"
     :innerText "[FADE]"
     :onclick
     (fn []
       [[:update-db (fn [db] (update db :toggle-visible not))]])}]])

;; =========================================================

;; (spec/def ::exclude (spec/coll-of string?))
;; (spec/def ::preferences (spec/keys :req-un [::exclude]))

;; (def def-config {:exclude []})

;; ;; (def ^:private db (atom {}))
;; (def db (atom {}))

;; (defn try-parse-db [input-str]
;;   (let [cfg (edn/read-string input-str)
;;         raw-prefs (spec/conform ::preferences cfg)]
;;     (if (spec/invalid? raw-prefs)
;;       nil
;;       {:config cfg :raw-config input-str})))

;; ;; =========================================================

;; (defn- contains-strings [s & subs]
;;   (let [low-s (str/lower-case s)
;;         low-subs (mapv (fn [x] (str/lower-case x)) subs)]
;;     (boolean (some (fn [x] (str/includes? low-s x)) low-subs))))

;; (defn skip-nodes [exclude nodes]
;;   (filter
;;    (fn [{title :title body :body :as node}]
;;      (let [sample (first (remove str/blank? [title body]))]
;;        (apply contains-strings sample exclude)))
;;    nodes))
