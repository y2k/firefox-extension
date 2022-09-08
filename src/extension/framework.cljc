(ns extension.framework
  (:require [extension.domain :as d]))

(def listeners (atom {}))

(def ^:private fx-handlers (atom {}))

(defn reset-all []
  (reset! listeners {}))

(defn reg-fx [fx-name handler]
  (println "[LOG][reg-fx]" fx-name)
  (swap!
   fx-handlers
   (fn [state]
     (update state fx-name (fn [handlers] (vec (conj handlers handler))))))
  nil)

(defn reg-event [event f]
  (swap! listeners
         (fn [state]
           (update state event (fn [xs] (vec (conj xs f))))))
  nil)

(defn reg-event-db [event f]
  (swap! listeners
         (fn [state]
           (update state event (fn [xs] (vec (conj xs (fn [e] (f @d/db e))))))))
  nil)

(comment
  (let [event-name :extension.extension/close-clicked event-param {}]
    (let [event-handler (event-name @listeners)]
      (println "event-handler:" event-handler)
      (if (nil? event-handler)
        (println "[LOG] EVENT handlers for" event-name "NOT FOUND")
        (doseq [event-handler event-handler]
          (let [event-result (event-handler event-param)]
            (println "event-result=" event-result)
            (if (some? event-result)
              (let [[fx-name fx-param] event-result]
                (let [handlers (fx-name @fx-handlers)]
                  handlers))))))))

  comment)

(defn dispatch [event-name event-param]
  (println (str "[LOG][dispatch]\n" event-name "\n" event-param))
  (let [event-handler (event-name @listeners)]
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
