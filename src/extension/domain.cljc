(ns extension.domain
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.spec.alpha :as spec]))

(spec/def ::exclude (spec/coll-of string?))
(spec/def ::preferences (spec/keys :req-un [::exclude]))

(def def-config {:exclude []})

(def db (atom {}))

(defn try-parse-db [input-str]
  (let [cfg (edn/read-string input-str)
        raw-prefs (spec/conform ::preferences cfg)]
    (if (spec/invalid? raw-prefs)
      nil
      {:config cfg :raw-config input-str})))

;; =========================================================

(defn- contains-strings [s & subs]
  (let [low-s (.toLowerCase s)]
    (boolean (some (fn [x] (str/includes? low-s x)) subs))))

(defn skip-nodes [exclude nodes]
  (filter
   (fn [{title :title body :body :as node}]
     (let [sample (first (remove str/blank? [title body]))]
       (apply contains-strings sample exclude)))
   nodes))

;; =========================================================

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
