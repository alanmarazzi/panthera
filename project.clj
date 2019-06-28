(defproject panthera "0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cnuernber/libpython-clj "0.10"]
                 [camel-snake-kebab "0.4.0"]
                 [org.clojure/core.memoize "0.7.2"]]
  :repl-options {:init-ns panthera.pandas})
