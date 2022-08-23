(defproject extension "0.1.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.9.1"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [org.clojure/core.async  "0.4.500"]]

  :plugins [[lein-figwheel "0.5.20"]
            [lein-cljsbuild "1.1.8" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id "extension"
                :source-paths ["src"]
                :figwheel {}
                :compiler {:main extension.extension
                           :asset-path "js/compiled/out_ext"
                           :output-to "resources/public/js/compiled/extension.js"
                           :output-dir "resources/public/js/compiled/out_ext"
                           :source-map-timestamp true}}
               {:id "options"
                :source-paths ["src"]
                :figwheel {}
                :compiler {:main extension.options
                           :asset-path "js/compiled/out_opt"
                           :output-to "resources/public/js/compiled/options.js"
                           :output-dir "resources/public/js/compiled/out_opt"
                           :source-map-timestamp true}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:main extension.extension
                           :output-to "resources/public/js/compiled/extension.js"
                           :optimizations :advanced
                           :output-dir "resources/public/js/compiled/out_min"
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles {:extension
             {:dependencies [[binaryage/devtools "1.0.0"]
                             [cider/piggieback "0.5.3"]
                             [figwheel-sidecar "0.5.20"]]
              :source-paths ["src" "dev"]
              :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                :target-path]}
             :options
             {:dependencies [[binaryage/devtools "1.0.0"]
                             [figwheel-sidecar "0.5.20"]]
              :source-paths ["src" "dev"]
              :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                :target-path]}})
