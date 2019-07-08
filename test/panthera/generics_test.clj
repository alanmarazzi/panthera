(ns panthera.generics-test
  (:require
    [clojure.test :refer :all]
    [libpython-clj.python :as py]
    [panthera.pandas.generics :as g]
    [panthera.pandas.utils :as u]))

(deftest series
  (are [i m]
    (u/series? (g/series i m))
    [] {}
    [] {:name :test}
    [1 2 3] {}
    1 {}
    ["1" "2"] {}
    ["1" "2"] {:dtype :float32})
  (are [i m o]
    (= (vec (g/series i m)) o)
    [] {} []
    [] {:name :test} []
    [1 2 3] {} [1 2 3]
    [:a :b] {} ["a" "b"]
    ["a" "b"] {} ["a" "b"]
    [1 2] {:dtype :str} ["1" "2"]
    ["1" "2"] {:dtype :float32} [1.0 2.0]))

(deftest data-frame
  (are [i m]
    (u/data-frame? (g/data-frame i m))
    [{:a 1 :b 2}] {}
    (to-array-2d [[1 2] [3 4]]) {}
    (to-array-2d [[1 2] [3 4]]) {:columns [:a :b]}
    (to-array-2d [[1 2] [3 4]]) {:dtype :int8})
  (are [i m o]
    (= (u/->clj (g/data-frame i m)) o)
    [] {} []
    [] {:columns [:a :b]} []
    [{:a 1 :b 2} {:a 1 :b 2}] {} [{:a 1 :b 2} {:a 1 :b 2}]
    [{:a "1" :b 2} {:a "3" :b 2}] {} [{:a "1" :b 2} {:a "3" :b 2}]

    [{:a "1" :b 2} {:a "3" :b 2}]
    {:dtype :float32}
    [{:a 1.0 :b 2.0} {:a 3.0 :b 2.0}]

    [{:a "1" :b 2} {:a "3" :b 2}]
    {:dtype :str}
    [{:a "1" :b "2"} {:a "3" :b "2"}]

    (to-array-2d [[1 2] [3 4]]) {} [{0 1 1 2} {0 3 1 4}]
    (to-array-2d [[1 2] [3 4]])
    {:columns [:a :b]} [{:a 1 :b 2} {:a 3 :b 4}]))

(deftest one-hot
  (are [i m o]
       (= (u/->clj (g/one-hot (g/series i) m)) o)
    [] {} []
    ["a" "b"] {} [{:a 1
                   :b 0}
                  {:a 0
                   :b 1}]
    ["a" "b"] {:prefix "pre"} [{:pre-a 1
                                :pre-b 0}
                               {:pre-a 0
                                :pre-b 1}])
  (are [i m o]
       (= (u/->clj (g/one-hot (g/data-frame i)
                              {:columns m})) o)

    [{:a 1 :b "c"} {:a 2 :b "d"}]
    [:b]
    [{:a   1
      :b-c 1
      :b-d 0}
     {:a   2
      :b-c 0
      :b-d 1}]

    [{:a 1 :b "c" :c 1} {:a 2 :b "d" :c 2}]
    [:b :c]
    [{:a   1
      :b-c 1
      :b-d 0
      :c-1 1
      :c-2 0}
     {:a   2
      :b-c 0
      :b-d 1
      :c-1 0
      :c-2 1}]))

(deftest unique
  (are [i o]
       (= (vec (g/unique i)) o)
    [] []
    [1 1] [1]
    [:a :b :a] ["a" "b"]
    [1 -1 1] [1 -1]))

(deftest index
  (are [i o]
       (= (vec (g/index i)) o)
    (g/series []) []
    (g/series [1 2 3]) [0 1 2]
    (g/series [1 2] {:index [100 1000]}) [100 1000]))

(deftest values
  (are [i o]
       (= (vec (g/values i)) o)
    (g/series []) []
    (g/series [1 2 3]) [1 2 3]
    (g/data-frame (to-array-2d [[1 2] [3 4]])) [[1 2] [3 4]]))

(deftest shape
  (are [i o]
       (= (vec (g/shape i)) o)
    (g/series []) [0]
    (g/series [1 2 3]) [3]
    (g/data-frame (to-array-2d [[1 2] [3 4]])) [2 2]))

(deftest hasnans?
  (are [i o]
       (= (g/hasnans? i) o)
    (g/series []) false
    (g/series [nil]) true
    (g/series [1 2 nil]) true))

(deftest subset-rows
  (are [s o]
       (= (u/->clj (apply g/subset-rows
                          (g/data-frame (->> (range 1 11)
                                             (partition 2)
                                             to-array-2d)) s)) o)
    [] (u/->clj (g/data-frame (->> (range 1 11)
                                   (partition 2)
                                   to-array-2d)))
    [1] [{0 1 1 2}]
    [1 3] [{0 3 1 4} {0 5 1 6}]
    [1 3 2] [{0 3 1 4}]))

(deftest cross-section
  (are [k o]
       (= (vec
           (g/cross-section
            (g/series (range 5)
                      {:index [:a :b :b :c :a]}) k))
          o)
    :a [0 4]
    :b [1 2]))

(deftest head
  (are [n o]
       (= (u/->clj
           (g/head
            (g/data-frame
             (flatten
              (repeat 5 [{:a 1 :b 2}
                         {:a 2 :b 3}]))) n))
          o)
    nil (drop-last (flatten
                    (repeat 3 [{:a 1 :b 2}
                               {:a 2 :b 3}])))
    1 [{:a 1 :b 2}]
    8 (flatten
       (repeat 4 [{:a 1 :b 2}
                  {:a 2 :b 3}]))))

(deftest n-largest
  (are [m o]
       (= (vec
           (g/n-largest
            (g/series (range 20)) m))
          o)
    {:n 5} (range 19 14 -1)
    {:n 3} [19 18 17]
    {:n 8} (range 19 11 -1)))

(deftest n-smallest
  (are [m o]
       (= (vec
           (g/n-smallest
            (g/series (range 20)) m))
          o)
    {:n 5} (range 5)
    {:n 3} (range 3)
    {:n 8} (range 8)))

(deftest n-unique
  (are [i o]
       (= (vec
           (g/n-unique
            (g/series i)))
          o)
    (range 10) 10
    [1 1 2] 2
    [11 nil 3] 3))