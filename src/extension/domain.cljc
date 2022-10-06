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

;; ====================== USER MENU ======================

(defn add-user-menu-begin []
  ["#navtopright" {}])

(defn fade-clicked [db _]
  [[:db (update db :css-variables (fn [vars]
                                    (assoc vars "--ext_content_opacity"
                                           (if (not= "0.05" (get vars "--ext_content_opacity"))
                                             "0.05"
                                             "1.0"))))]])

(defn add-user-menu-end [_ entities]
  [[:add-node
    {:target (:node (nth entities 0))
     :node [:a {:innerText "[FADE]"
                :onclick #'fade-clicked}]}]])

;; =========================================================

(defn handle-media-changes-begin []
  ["video.expandedWebm:not(.ext-marked)"
   {}])

(defn media-clicked [parent-node e]
  [[:remove-node (:target e)]
   [:click
    {:type :selector
     :target parent-node
     :selector ".collapseWebm > a"}]])

(defn handle-media-changes-end [_ entities]
  (mapcat
   (fn [entity]
     (let [parent-node {:type :parent
                        :target (:node entity)}]
       [[:add-class
         {:target (:node entity)
          :class "ext-marked"}]
        [:add-node
         {:target parent-node
          :node [:div {:class "ext-hover"
                       :onclick (fn [_ e] (media-clicked parent-node e))}]}]]))
   entities))

;; ====================== EXTENSION OPTIONS ======================

(defn make-options [id db parent-node]
  [[:add-node
    {:target parent-node
     :node
     [:div {}
      [:div {:className "panel-section panel-section-formElements"}
       [:div {:className "panel-formElements-item"}
        [:textarea
         {:onchange (fn [db e] [[:db (update-in db id (fn [db] (assoc db :input (:value (:target e)))))]])
          :value (-> db (:exclude) (str))
          :className "browser-style text1"}]]]
      [:footer {:className "panel-section panel-section-footer"}
       [:button {:className "panel-section-footer-button"
                 :innerText "Cancel"
                 :onclick (fn [db]
                            [[:set-value {:target {:type :selector :target parent-node :selector ".text1"}
                                          :value (-> db (:exclude) (str))}]])}]
       [:div {:className "panel-section-footer-separator"}]
       [:button {:className "panel-section-footer-button default"
                 :innerText "Confirm"
                 :onclick (fn [db]
                            (let [cfg (edn/read-string (:input (get-in db id)))]
                              [[:db (assoc db :exclude cfg)]]))}]]]}]])
