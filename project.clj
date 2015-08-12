(defproject cats/cats-channel "0.2.0-SNAPSHOT"
  :description  "Category Theory abstractions for Clojure"
  :url          "https://github.com/funcool/canal"
  :license      {:name "BSD (2 Clause)"
                 :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.7.0" :scope "provided"]
                 [org.clojure/clojurescript "1.7.48" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [funcool/cats "0.6.1"]]
  :deploy-repositories {"releases"  :clojars
                        "snapshots" :clojars}

  :source-paths ["src"]
  :test-paths   ["test"]

  ;; :cljsbuild {:test-commands {"test" ["node" "output/tests.js"]}
  ;;             :builds [{:id "dev"
  ;;                       :source-paths ["output/test/cljs" "output/src"]
  ;;                       :notify-command ["node" "output/tests.js"]
  ;;                       :compiler {:output-to "output/tests.js"
  ;;                                  :output-dir "output/out"
  ;;                                  :source-map true
  ;;                                  :static-fns true
  ;;                                  :cache-analysis false
  ;;                                  :main cats.monad.channel-spec
  ;;                                  :optimizations :none
  ;;                                  :target :nodejs
  ;;                                  :pretty-print true}}]}

  :jar-exclusions [#"\.swp|\.swo|user.clj"]

  :profiles {:dev {:dependencies [[funcool/cljs-testrunners "0.1.0-SNAPSHOT"]]
                   :plugins      [[lein-cljsbuild "1.0.4"]]}})
