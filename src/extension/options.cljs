(ns extension.options
  (:require [extension.domain :as d]
            [extension.storage :as s]
            [clojure.edn :as edn]
            [extension.extension :as ext]))

(comment

  (defn make-options [db parent-node]
    [[:add-element
      {:target parent-node
       :tag "DIV"
       :children
       [{:tag "DIV"
         :class "panel-section panel-section-formElements"
         :children
         [{:tag "DIV"
           :class "panel-formElements-item"
           :children
           [{:tag "TEXTAREA"
             :onchange (fn [db e] [[:db (assoc db :input (:value (:target e)))]])
             :value (-> db (:exclude) (str))
             :class "browser-style text1"}]}]}
        {:tag "FOOTER"
         :class "panel-section panel-section-footer"
         :children
         [{:tag "BUTTON"
           :class "panel-section-footer-button"
           :innerText "Cancel"
           :onclick (fn [db]
                      [[:set-value {:target {:type :selector :target parent-node :selector ".text1"}
                                    :value (-> db (:exclude) (str))}]])}
          {:tag "DIV"
           :class "panel-section-footer-separator"}
          {:tag "BUTTON"
           :class "panel-section-footer-button default"
           :innerText "Confirm"
           :onclick (fn [db]
                      (let [cfg (edn/read-string (:input db))]
                        [[:db (assoc db :exclude cfg)]]))}]}]}]])

  (let [node (.getElementById js/document "root")]
    (doseq [ch node.children] (.remove ch))
    (doseq [cmd (make-options (d/get-db) {:type :node :raw-node node})]
      (ext/execute-command cmd)))

  ())

(defn- update-text-from-db []
  (->>
   (d/get-db)
   (:exclude)
   (str)
   (set! (.-value (.getElementById js/document "options-text")))))

(defn- document-loaded []
  (update-text-from-db))

(defn- save-config []
  (let [input-str (.-value (.getElementById js/document "options-text"))
        cfg (edn/read-string input-str)]
    (d/set-db {:exclude cfg})
    (s/save-prefs)
    (update-text-from-db)))

(defn- cancel-config []
  (update-text-from-db))

;; (defonce setup
;;   (do
;;     (s/load-prefs #'document-loaded)
;;     (set! (.-onclick (.querySelector js/document "#confirm")) #(save-config))
;;     (set! (.-onclick (.querySelector js/document "#cancel")) #(cancel-config))
;;     (document-loaded)
;;     nil))
