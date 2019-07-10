(ns panthera.conversion-test
  (:require
   [clojure.test :refer :all]
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]
   [panthera.pandas.conversion :as c]))

(deftest ->numeric
  (are [i o]
      (= (vec (c/->numeric i)) o)
    [:1 :2 :3] [1 2 3]
    ["1.3" 1 "-2"] [1.3 1.0 -2.0]))

(deftest ->datetime
  (are [i m o]
      (= (str (c/->datetime i m)) o)
    (* 1192492800 1e9) {} "2007-10-16 00:00:00"
    1192492800 {:unit :s} "2007-10-16 00:00:00"
    "2007-10-16 00:00:00" {} "2007-10-16 00:00:00"
    "2007-10-16" {} "2007-10-16 00:00:00"))

(deftest ->timedelta
  (are [i m o]
      (= (str (c/->timedelta i m)) o)
    1 {:unit :s} "0 days 00:00:01"
    1 {:unit :d} "1 days 00:00:00"
    "2 days" {}  "2 days 00:00:00"))
