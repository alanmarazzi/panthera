(ns panthera.utils-test
  (:require
   [clojure.test :refer :all]
   [panthera.config :refer [start-python!]]
    [libpython-clj.python :as py]
    [panthera.pandas.utils :as u]))

(use-fixtures :once start-python!)

(deftest pytype
  (are [t d]
    (identical? t (u/pytype d))
    :list (py/->py-list [])
    :list (py/->py-list [-1])
    :list (py/->py-list [1 2 3])
    :list (py/->py-list [[1 2] [3 4]])
    :tuple (py/->py-tuple [])
    :tuple (py/->py-tuple [0])
    :tuple (py/->py-tuple [1 2 3])
    :tuple (py/->py-tuple [[1 2] [3 4]])
    :dict (py/->py-dict {})
    :dict (py/->py-dict {:a 1 :b "2" :c [1 2 3]})
    :dict (py/->py-dict {"a" 1})))

(deftest slice
  (are [d]
    (identical? :slice (u/pytype (apply u/slice d)))
    []
    [nil]
    [1]
    [1 2]
    [1 2 3]
    [3 7 2])
  (are [s res]
    (= (py/->jvm 
        (py/get-item 
         (py/->py-list (range 4)) s)) res)
    (u/slice) (vec (range 4))
    (u/slice 2) [0 1]
    (u/slice 1 3) [1 2]
    (u/slice -1) [0 1 2]
    (u/slice 0 5 2) [0 2]))

(deftest keys->pyargs
  (are [i o]
    (= (u/keys->pyargs i) o)
    {} {}
    {:a 1} {"a" 1}
    {:a 1 :b 2} {"a" 1 "b" 2}
    {:a-k 1} {"a_k" 1}))

(deftest memo-columns-converter
  (are [i o]
      (= (u/memo-columns-converter i) o)
    1 1
    nil nil
    "a" :a
    "col_1" :col-1
    ["multi" "col"] [:multi :col]
    "ALL_CAPS" :ALL-CAPS
    "WeIrD_caPs" :WeIrD-caPs))

(deftest ->clj
  (is (= (u/->clj
           (py/call-attr u/pd "DataFrame" [{:a 1 :b 2} {:a 3 :b 4}]))
         [{:a 1 :b 2} {:a 3 :b 4}]))
  (is (= (u/->clj
           (py/call-attr u/pd "Series" [1 2 3]))
         [{:unnamed 1} {:unnamed 2} {:unnamed 3}]))
  (is (= (u/->clj
           (py/call-attr-kw u/pd "Series" [[1 2 3]] {"name" "test"}))
         [{:test 1} {:test 2} {:test 3}])))
