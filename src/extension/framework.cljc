(ns extension.framework
  (:require [extension.domain :as d]))

(def listeners (atom {}))

(defn reset-all []
  (reset! listeners {}))

;; event -> (params -> unit) -> unit
(defn reg-event [event f]
  (swap! listeners
         (fn [state]
           (update state event (fn [xs] (vec (conj xs f))))))
  nil)
  ;; event -> (params -> unit) -> unit
(defn reg-event-db [event f]
  (swap! listeners
         (fn [state]
           (update state event (fn [xs] (vec (conj xs (fn [e] (f @d/db e))))))))
  nil)

;; (symbol * obj) list -> unit
(defn dispatch [events]
  (doseq [[e ep] (partition 2 events)]
    (println "[EVENT][" e "]" ep)
    (doseq [f (e @listeners)]
      (f ep))))

(defn dispatch2 [e ep]
  (doseq [f (e @listeners)]
    (f ep)))
