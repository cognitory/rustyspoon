(defproject rustyspoon-tutorial "0.0.1"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.494"]
                 [cljs-ajax "0.5.3"]
                 [re-frame "0.9.2"]
                 [venantius/accountant "0.1.9"]
                 [secretary "1.2.3"]
                 [markdown-clj "0.9.86"]
                 [garden "1.3.2"]
                 [fipp "0.6.4"]
                 [cljsjs/highlight "8.4-0"]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.5.0-6"]]

  :clean-targets ^{:protect false}
  ["resources/public/js"]

  :figwheel {:server-port 3450
             :css-dirs ["resources/public/css"]}

  :cljsbuild {:builds
              [{:id "dev"
                :figwheel true
                :source-paths ["src"]
                :compiler {:main rustyspoon-tutorial.orbit
                           :asset-path "/js/dev"
                           :output-to "resources/public/js/rustyspoon-tutorial.js"
                           :output-dir "resources/public/js/dev"
                           :verbose true}}

               {:id "prod"
                :figwheel true
                :source-paths ["src"]
                :compiler {:main rustyspoon-tutorial.orbit
                           :asset-path "/js/prod"
                           :output-to "resources/public/js/rustyspoon-tutorial.js"
                           :output-dir "resources/public/js/prod"
                           :optimizations :simple
                           :pretty-print false}}]}

  :min-lein-version "2.5.0"
  )
