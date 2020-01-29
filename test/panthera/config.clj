(ns panthera.config
  (:require
   [libpython-clj.python :as py]))

(defn start-python!
  [f]
  (py/initialize!)
  (f))
