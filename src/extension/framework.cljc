(ns extension.framework
  (:require [extension.domain :as d]))

(def ^:private event-handlers (atom {}))

(def ^:private fx-handlers (atom {}))

(defn reset-all []
  (reset! event-handlers {}))

(defn reg-fx [fx-name handler]
  (println "[LOG][reg-fx]" fx-name)
  (swap!
   fx-handlers
   (fn [state]
     (update state fx-name (fn [handlers] (vec (conj handlers handler))))))
  nil)

(defn reg-event [event f]
  (swap! event-handlers
         (fn [state]
           (update state event (fn [xs] (vec (conj xs f))))))
  nil)

(defn reg-event-db [event f]
  (swap! event-handlers
         (fn [state]
           (update state event (fn [xs] (vec (conj xs (fn [e] (f @d/db e))))))))
  nil)

(defn dispatch [event-name event-param]
  (println (str "[LOG][dispatch]\n" event-name "\n" event-param))
  (let [event-handler (event-name @event-handlers)]
    (if (nil? event-handler)
      (println "[LOG] EVENT handlers for" event-name "NOT FOUND")
      (doseq [event-handler event-handler]
        (let [event-result (event-handler event-param)]
          (if (some? event-result)
            (let [[fx-name fx-param] event-result]
              (let [handlers (fx-name @fx-handlers)]
                (if (nil? handlers)
                  (println "[LOG] EFFECT handlers for" fx-name "NOT FOUND")
                  (do
                    (println "[LOG] call effect: " fx-name "with param:" fx-param)
                    (doseq [fx-handler handlers]
                      (fx-handler fx-param))))))))))))

(reg-fx
 :batch
 (fn [effects]
   (doseq [event-result effects]
     (let [[fx-name fx-param] event-result]
       (let [handlers (fx-name @fx-handlers)]
         (if (nil? handlers)
           (println "[LOG] EFFECT handlers for" fx-name "NOT FOUND")
           (do
             (println "[LOG] call effect: " fx-name "with param:" fx-param)
             (doseq [fx-handler handlers]
               (fx-handler fx-param)))))))))
