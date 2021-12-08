(ns extension.effects
  (:require [extension.domain :as d]
            [clojure.edn :as edn]))

(defn init []
  (->
   (js/browser.storage.local.get "key")
   (.then
    (fn [x]
      (let [p (some-> (.-key x) (.-value))
            db {:raw-config p
                :config (if (nil? p) d/def-config (edn/read-string p))}]
        (reset! d/db db)
        (d/dispatch [:extension.domain/db-changed nil]))))))

(do
  (d/reg-event-fx
   :extension.domain/db-reseted
   (fn [db]
     (reset! d/db db)
     (js/browser.storage.local.set (clj->js {:key {:value (:raw-config db)}}))
     (d/dispatch [:extension.domain/db-changed nil]))))
