(defproject panthera "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [t6/from-scala "0.3.0"]
                 [org.scala-saddle/saddle-core_2.11 "1.3.4"]
                 [org.scala-lang/scala-library "2.11.6"]]
  :repl-options {:init-ns panthera.core})
