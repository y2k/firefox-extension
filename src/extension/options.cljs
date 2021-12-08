(ns extension.options
  (:require [extension.domain :as d]
            [extension.effects :as eff]))

;; Utils

(defn- on-click [id f]
  (set! (.-onclick (.getElementById js/document id)) f))

(defn- read-option [] (.getElementById js/document "options-text"))

;; Screen

(defn save-prefs []
  (let [db (d/try-parse-db (.-value (read-option)))]
    (if (nil? db)
      (js/alert "Invalid config")
      (d/dispatch [:extension.domain/db-reseted db]))))

(defn reload-config []
  (set! (.-value (read-option)) (:raw-config @d/db)))

(do
  (on-click "confirm" save-prefs)
  (on-click "cancel" reload-config))

(do
  (d/reg-event-fx :extension.domain/db-changed (fn [_] (reload-config)))
  (eff/init))
