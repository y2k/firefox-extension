(ns extension.options
  (:require [extension.domain :as d]
            [extension.framework :as f]
            [extension.effects :as eff]))

;; Utils

(defn- on-click [id f]
  (set! (.-onclick (.getElementById js/document id)) f))

(defn- read-option [] (.getElementById js/document "options-text"))

;; Common CLJ

(f/reg-event :extension.domain/db-changed #(f/dispatch [::init nil]))

(f/reg-event
 ::init (fn [] (f/dispatch [::update-ui (:raw-config @d/db)])))

(f/reg-event
 ::confirm (fn []
             (let [db (d/try-parse-db (.-value (read-option)))]
               (if (nil? db)
                 (f/dispatch [::show-alert "Invalid config"
                              ::update-ui (:raw-config @d/db)])
                 (f/dispatch [:extension.domain/db-reset-requested db])))))

;; JS CLJ

(do
  (f/reg-event ::update-ui (fn [msg] (set! (.-value (read-option)) msg)))
  (f/reg-event ::show-alert (fn [msg] (js/alert msg)))
  (f/dispatch [::init nil])
  (eff/init)

  (->>
   (.querySelectorAll js/document "button[x-event]")
   (run! (fn [node] (set! (.-onclick node) #(f/dispatch [(keyword (.getAttribute node "x-event")) nil]))))))
