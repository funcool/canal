(defproject cats/cats-channel "0.1.0"
  :description "Category Theory abstractions for Clojure"
  :url "https://github.com/funcool/cats-channel"
  :license {:name "BSD (2 Clause)"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cats "0.4.0"]]
  :deploy-repositories {"releases" :clojars
                        "snapshots" :clojars}

  :source-paths ["output/src"]
  :test-paths ["output/test/clj"]

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "output/src"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "output/src"
                   :rules :cljs}
                  {:source-paths ["test"]
                   :output-path "output/test/clj"
                   :rules :clj}
                  {:source-paths ["test"]
                   :output-path "output/test/cljs"
                   :rules :cljs}]}

  :cljsbuild {:test-commands {"test" ["node" "output/tests.js"]}
              :builds [{:id "dev"
                        :source-paths ["output/test/cljs" "output/src"]
                        :notify-command ["node" "output/tests.js"]
                        :compiler {:output-to "output/tests.js"
                                   :output-dir "output/out"
                                   :source-map true
                                   :static-fns true
                                   :cache-analysis false
                                   :main cats.monad.channel-spec
                                   :optimizations :none
                                   :target :nodejs
                                   :pretty-print true}}]}

  :jar-exclusions [#"\.cljx|\.swp|\.swo|user.clj"]

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]
                                  [org.clojure/clojurescript "0.0-3126"]
                                  [funcool/cljs-testrunners "0.1.0-SNAPSHOT"]]
                   :plugins [[org.clojars.cemerick/cljx "0.6.0-SNAPSHOT"
                              :exclusions [org.clojure/clojure]]
                             [lein-cljsbuild "1.0.4"]]}})
