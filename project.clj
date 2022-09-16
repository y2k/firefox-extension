(defproject extension "0.1.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.9.1"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [org.clojure/core.async  "0.4.500"]
                 [cider/cider-nrepl "0.25.11"]
                 [cider/piggieback "0.5.3"]
                 [figwheel-sidecar "0.5.20"]]

  :plugins [[lein-figwheel "0.5.20"]
            [lein-cljsbuild "1.1.8" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id "extension"
                :source-paths ["src"]
                :figwheel {}
                :compiler {:main extension.extension
                           :asset-path "http://localhost:3449/js/out_ext"
                           :output-to "resources/js/extension.js"
                           :output-dir "resources/js/out_ext"
                           :source-map-timestamp true}}
               {:id "options"
                :source-paths ["src"]
                :figwheel {}
                :compiler {:main extension.options
                           :asset-path "js/out_opt"
                           :output-to "resources/js/options.js"
                           :output-dir "resources/js/out_opt"
                           :source-map-timestamp true}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:main extension.extension
                           :output-to "resources/js/extension.js"
                           :optimizations :advanced
                           :output-dir "resources/js/out_min"
                           :pretty-print false}}
               {:id "min-options"
                :source-paths ["src"]
                :compiler {:main extension.options
                           :output-to "resources/js/options.js"
                           :optimizations :advanced
                           :output-dir "resources/js/out_min_options"
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/css"]
             :nrepl-port 7888
             :hawk-options {:watcher :polling}
             :nrepl-middleware ["cider.nrepl/cider-middleware"
                                "refactor-nrepl.middleware/wrap-refactor"
                                "cemerick.piggieback/wrap-cljs-repl"]}

  :profiles {:extension
             {:dependencies [[binaryage/devtools "1.0.0"]
                             [cider/piggieback "0.5.3"]
                             [figwheel-sidecar "0.5.20"]]
              :source-paths ["src" "dev"]
              :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
              :clean-targets ^{:protect false} ["resources/js/compiled"
                                                :target-path]}
             :options
             {:dependencies [[binaryage/devtools "1.0.0"]
                             [figwheel-sidecar "0.5.20"]]
              :source-paths ["src" "dev"]
              :clean-targets ^{:protect false} ["resources/js/compiled"
                                                :target-path]}})
