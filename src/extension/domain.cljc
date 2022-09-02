(ns extension.domain
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.spec.alpha :as spec]))

(spec/def ::exclude (spec/coll-of string?))
(spec/def ::preferences (spec/keys :req-un [::exclude]))

(def def-config {:exclude []})

;; (def ^:private db (atom {}))
(def db (atom {}))

(defn try-parse-db [input-str]
  (let [cfg (edn/read-string input-str)
        raw-prefs (spec/conform ::preferences cfg)]
    (if (spec/invalid? raw-prefs)
      nil
      {:config cfg :raw-config input-str})))

;; =========================================================

(comment

  (type 0)
  (use 'figwheel-sidecar.repl-api)
  (cljs-repl)

  (use 'figwheel-sidecar.repl-api)
  ;; (start-figwheel!)
  (cljs-repl)
  (js/alert "test")

  comment)

(defn- contains-strings [s & subs]
  (let [low-s (.toLowerCase s)
        low-subs (mapv (fn [x] (.toLowerCase x)) subs)]
    (boolean (some (fn [x] (str/includes? low-s x)) low-subs))))

(defn skip-nodes [exclude nodes]
  (filter
   (fn [{title :title body :body :as node}]
     (let [sample (first (remove str/blank? [title body]))]
       (apply contains-strings sample exclude)))
   nodes))
