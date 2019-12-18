(ns panthera.generics-test
  (:require
    [clojure.test :refer :all]
    [libpython-clj.python :as py]
    [panthera.pandas.generics :as g :reload true]
    [panthera.pandas.utils :as u :reload true]
    [panthera.pandas.math :as m]))

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
    (g/series [1 2 3]) [1 2 3])
  (is (= (mapv vec (g/values (g/data-frame (to-array-2d [[1 2] [3 4]]))))
         [[1 2] [3 4]])))

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

(deftest subset-cols
  (are [i cols o]
       (= (u/->clj
           (apply
            g/subset-cols
            (g/data-frame i)
            cols))
          o)
    [{:a 1}] [:a] [{:a 1}]
    [{:a 1 :b 2 :c 3}] [:a :c] [{:a 1 :c 3}]
    (vec (repeat 5 {:a 1 :b 2})) [:b] (vec (repeat 5 {:b 2}))
    [{:wEiR__.D 1 :b 2}] [:wEiR__.D] [{:w-ei-r-.-d 1}]))

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
       (= (g/n-unique
           (g/series i))
          o)
    (range 10) 10
    [1 1 2] 2
    [11 nil 3] 2))

(deftest unique?
  (are [i o]
       (= (g/unique? i) o)
    [] true
    [1 2 3] true
    [1 1] false
    [-1 1] true
    [1 nil] true
    ["a" "b"] true
    (g/series [1 1]) false))

(deftest increasing?
  (are [i o]
       (= (g/increasing? i) o)
    [] true
    [1 5 9] true
    [1 nil 3] false
    [1 1 1 1] true
    [3 2 1] false))

(deftest decreasing?
  (are [i o]
       (= (g/decreasing? i) o)
    [] true
    [9 7 1] true
    [3 nil 1] false
    [3 3 3] true
    [1 2 3] false))

(deftest value-counts
  (are [i m o]
       (= (g/value-counts i (merge {:clj true} m)) o)
    [] {} {}
    [1 1 2] {} {1 2 2 1}
    [:a :a :b :c] {} {:a 2 :b 1 :c 1}
    (repeat 50 :a) {} {:a 50}
    [:a :a :b :c] {:normalize true} {:a 0.5 :b 0.25 :c 0.25}))

(deftest reset-index
  (are [i m o]
      (= (u/->clj (g/reset-index (g/series i) m)) o)
    (range 3) {} [{:index 0 0 0}
                  {:index 1 0 1}
                  {:index 2 0 2}]
    (range 3) {:drop true} [{:unnamed 0}
                            {:unnamed 1}
                            {:unnamed 2}]
    (range 3) {:name "col"} [{:index 0 :col 0}
                             {:index 1 :col 1}
                             {:index 2 :col 2}]))

(deftest names
  (are [i o]
      (= (g/names i) o)
    (g/series [1 2]) nil
    (g/series [1 2] {:name "name"}) "name"
    (g/series [1 2] {:name :my-name}) "my-name")
  (are [i o]
      (= (vec (g/names (g/data-frame i))) o)
    [{:a 1 :b 2}] ["a" "b"]
    [{"a name" 1 :c 2}] ["a name" "c"]
    [{123 1 1/5 3}] [123.0 0.2]))

(deftest filter-rows
  (are [i b o]
      (= (u/->clj
          (g/filter-rows i b)) o)
    (g/series (range 10)) #(m/gt % 5) [{:unnamed 6}
                                       {:unnamed 7}
                                       {:unnamed 8}
                                       {:unnamed 9}]
    (g/series (range 4)) [false true false true] [{:unnamed 1}
                                                  {:unnamed 3}]

    (g/data-frame [{:a 1 :b 2}
                   {:a 3 :b 4}])
    #(-> %
         (g/subset-cols :a)
         (m/lt 3)
         g/values)
    [{:a 1 :b 2}]

    (g/data-frame [{:a 1 :b 2}
                   {:a 3 :b 4}
                   {:a 4 :b 5}])
    [true false false]
    [{:a 1 :b 2}]))

(deftest tail
  (are [i n o]
      (= (u/->clj
          (g/tail i n))
         o)
    (g/series (range 20)) nil [{:unnamed 15}
                               {:unnamed 16}
                               {:unnamed 17}
                               {:unnamed 18}
                               {:unnamed 19}]
    (g/series (range 20)) 2 [{:unnamed 18} {:unnamed 19}]
    (g/data-frame (vec (repeat 10 {:a 1 :b 2}))) nil (repeat 5 {:a 1 :b 2})
    (g/data-frame (vec (repeat 10 {:a 1 :b 2}))) 2 (repeat 2 {:a 1 :b 2})))

(deftest fill-na
  (are [v m o]
      (= (vec
          (g/fill-na (g/series [1 nil 2 nil]) v m)) o)
    3 {} [1.0 3.0 2.0 3.0]
    "a" {} [1.0 "a" 2.0 "a"]
    nil {:method :ffill} [1.0 1.0 2.0 2.0]))

(deftest select-rows
  (are [i id l h o]
       (= (u/->clj
           (g/select-rows
            (g/data-frame i (or {:index l} {}))
            id h))
          o)
    (to-array-2d (partition 2 (range 20)))
    []
    nil
    nil
    []

    (to-array-2d (partition 2 (range 20)))
    [0 3]
    nil
    nil
    [{0 0 1 1} {0 6 1 7}]

    (to-array-2d (partition 2 (range 10)))
    [0 3]
    [:a :b :c :d :e]
    nil
    [{0 0 1 1} {0 6 1 7}]

    (to-array-2d (partition 2 (range 10)))
    [0 3]
    nil
    :loc
    [{0 0 1 1} {0 6 1 7}]

    (to-array-2d (partition 2 (range 10)))
    [:a :d]
    [:a :b :c :d :e]
    :loc
    [{0 0 1 1} {0 6 1 7}]

    (to-array-2d (partition 2 (range 10)))
    (u/slice 3)
    nil
    nil
    [{0 0 1 1} {0 2 1 3} {0 4 1 5}]
    
    (to-array-2d (partition 4 (range 20)))
    [(u/slice 2) (u/slice 1)]
    nil
    :loc
    [{0 0 1 1} {0 4 1 5} {0 8 1 9}]))

(deftest set-index
  (are [idx m oid ov]
      (and (= (vec
               (g/index
                (g/set-index
                 (g/data-frame [{:a 1 :b 2 :c 3} {:a 2 :b 3 :c 4}])
                 idx m)))
              oid)
           (= (u/->clj
               (g/set-index
                (g/data-frame [{:a 1 :b 2 :c 3} {:a 2 :b 3 :c 4}])
                idx m))
              ov))
    [:a] {} [1 2] [{:b 2 :c 3} {:b 3 :c 4}]
    [:a :b] {} [[1 2] [2 3]] [{:c 3} {:c 4}]
    [:a] {:drop false} [1 2] [{:a 1 :b 2 :c 3} {:a 2 :b 3 :c 4}]
    [:a] {:append true} [[0 1] [1 2]] [{:b 2 :c 3} {:b 3 :c 4}]))
