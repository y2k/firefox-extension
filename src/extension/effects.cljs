(ns extension.effects
  (:require [extension.domain :as d]
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
          (d/dispatch [:extension.domain/db-changed nil])))))
    (do
      (reset! d/db {:raw-config "{}"
                    :config {:exclude ["READ"]}})
      (d/dispatch [:extension.domain/db-changed nil]))))

(do
  (d/reg-event-fx
   :extension.domain/db-reset-requested
   (fn [db]
     (reset! d/db db)
     (js/browser.storage.local.set (clj->js {:key {:value (:raw-config db)}}))
     (d/dispatch [:extension.domain/db-changed nil]))))
