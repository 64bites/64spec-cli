(defproject cli-64spec "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
  				 [org.clojure/core.async "0.2.374"]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                 [me.raynes/fs "1.4.6"]]
  :main ^:skip-aot cli-64spec.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
