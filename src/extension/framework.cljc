(ns extension.framework)

(def listeners (atom {}))

(defn reset-all []
  (reset! listeners {}))

;; event -> (params -> unit) -> unit
(defn reg-event-fx [event f]
  (swap! listeners
         (fn [state]
           (update state event (fn [xs] (vec (conj xs f))))))
  nil)

;; (symbol * obj) list -> unit
(defn dispatch [events]
  (doseq [[e ep] (partition 2 events)]
    (println "[EVENT][" e "]" ep)
    (doseq [f (e @listeners)]
      (f ep))))
