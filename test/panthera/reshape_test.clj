(ns panthera.reshape-test
  (:require
   [clojure.test :refer :all]
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]
   [panthera.pandas.generics :as g]))

(deftest crosstab
  (are [r c o]
      (= (u/->clj ))))
