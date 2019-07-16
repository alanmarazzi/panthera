(ns panthera.conversion-test
  (:require
   [clojure.test :refer :all]
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]
   [panthera.pandas.generics :as g]
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

(deftest date-range
  (are [m o]
      (= (map str (c/date-range m))
         o))

  {:start "2019-1" :end "2019-3" :freq "m"}
  ["2019-01-31 00:00:00" "2019-02-28 00:00:00" "2019-03-31 00:00:00"]

  {:start "2019-1" :end "2019-3" :periods 3}
  ["2019-01-01 00:00:00" "2019-02-15 00:00:00" "2019-04-01 00:00:00"])

(deftest timedelta-range
  (are [m o]
      (= (map str (c/timedelta-range m))
         o))

  {:start "1 hours" :end "2 days"}
  ["0 days 01:00:00" "1 days 01:00:00" "2 days 01:00:00"]

  {:start "1 hours" :end "4 hours" :freq "H"}
  ["0 days 01:00:00" "0 days 02:00:00" "0 days 03:00:00" "0 days 04:00:00"])

(deftest infer-time-freq
  (is (= (c/infer-time-freq ["2017" "2018" "2019"])
         "AS-JAN")))

(deftest astype
  (are [i d]
      (= (str (g/dtype (c/astype (g/series i) d)))
         (name d))
    [1 2 3] :float32
    [1 2 3] :int64))
