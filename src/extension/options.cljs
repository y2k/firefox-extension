(ns extension.options
  (:require [extension.domain :as d]
            [extension.effects :as eff]))

;; Utils

(defn- on-click [id f]
  (set! (.-onclick (.getElementById js/document id)) f))

(defn- read-option [] (.getElementById js/document "options-text"))

;; Common CLJ

(d/reg-event-fx :extension.domain/db-changed #(d/dispatch [::init nil]))

(d/reg-event-fx
 ::init (fn [] (d/dispatch [::update-ui (:raw-config @d/db)])))

(d/reg-event-fx
 ::confirm (fn []
             (let [db (d/try-parse-db (.-value (read-option)))]
               (if (nil? db)
                 (d/dispatch [::show-alert "Invalid config"
                              ::update-ui (:raw-config @d/db)])
                 (d/dispatch [:extension.domain/db-reset-requested db])))))

;; JS CLJ

(do
  (d/reg-event-fx ::update-ui (fn [msg] (set! (.-value (read-option)) msg)))
  (d/reg-event-fx ::show-alert (fn [msg] (js/alert msg)))
  (d/dispatch [::init nil])
  (eff/init)

  (->>
   (.querySelectorAll js/document "button[x-event]")
   (run! (fn [node] (set! (.-onclick node) #(d/dispatch [(keyword (.getAttribute node "x-event")) nil]))))))
