(ns panthera.math-test
  (:require
   [clojure.test :refer :all]
   [panthera.pandas.math :as m]
   [panthera.pandas.generics :as g]
   [panthera.pandas.utils :as u]))

(deftest base-math
  (are [op other out]
      (= (vec ((#'m/base-math op) (g/series [1 2 3]) other))
         out)
    :+ 1 [2 3 4]
    :+ 1/2 [1.5 2.5 3.5]
    :+ (g/series [1 2 3]) [2 4 6]

    :- 1 [0 1 2]
    :- 1/2 [0.5 1.5 2.5]
    :- (g/series [1 2 3]) [0 0 0]

    :* 2 [2 4 6]
    :* 1/2 [0.5 1.0 1.5]
    :* (g/series [3 3 3]) [3 6 9]

    :div 2 [0.5 1.0 1.5]
    :div 1/2 [2.0 4.0 6.0]
    :div (g/series [2 2 1]) [0.5 1.0 3.0]

    :fld 2 [0 1 1]
    :fld 1/2 [2.0 4.0 6.0]
    :fld (g/series [2 2 1]) [0 1 3]

    :mod 2 [1 0 1]
    :mod 1/2 [0.0 0.0 0.0]
    :mod (g/series [1 2 3]) [0 0 0]

    :** 2 [1 4 9]
    :** 4 [1 16 81]
    :** (g/series [1 2 3]) [1 4 27]

    :< 2 [true false false]
    :< 1/2 [false false false]
    :< (g/series [2 3 4]) [true true true]

    :> 2 [false false true]
    :> 1/2 [true true true]
    :> (g/series [0 1 2]) [true true true]

    :<= 2 [true true false]
    :<= 1/2 [false false false]
    :<= (g/series [1 3 2]) [true true false]

    :>= 2 [false true true]
    :>= 1/2 [true true true]
    :>= (g/series [1 3 2]) [true false true]

    :!= 2 [true false true]
    :!= 1/2 [true true true]
    :!= (g/series [1 3 2]) [false true true]

    := 2 [false true false]
    := 4/2 [false true false]
    := (g/series [1 3 2]) [true false false])

  (is (= (m/dot (g/series [1 2 3]) (g/series [1 2 3])) 14)))

(deftest abs
  (are [i o]
      (= (vec (m/abs (g/series i)))
         o)
    [1 2 3] [1 2 3]
    [-1 2] [1 2]
    [-3.5 4.7 1] [3.5 4.7 1.0]))

(deftest autocorr
  (are [i l o]
      (= (Math/round (m/autocorr (g/series i) l)) o)
    [1 2 3] nil 1
    [3 2 1] nil 1
    (range 10) 2 1
    [1 100 3 765 12 6] nil 0 
    [1 2 100 101] 2 1))

(deftest between
  (are [l r i o]
      (= (m/sum (m/between (g/series (range 100)) l r i)) o)
    1 10 true 10
    1 10 false 8
    20 75 true 56))

(deftest clip
  (are [i a o]
      (= (vec (m/clip (g/series i) a)) o)
    [-5 0 5] {:lower 0 :upper 0} [0 0 0]
    [1 5 9] {:lower 3 :upper 7} [3 5 7]))

(deftest corr
  (is (= (m/corr (g/series [1 2 3]) (g/series [3 2 1])) -1.0))
  (is (= (u/->clj (m/corr (g/data-frame (to-array-2d [[1 2] [3 4] [5 6]]))))
         [{0 1.0, 1 1.0} {0 1.0, 1 1.0}])))

(deftest cnt
  (is (= (m/cnt (g/series [1 nil 2])) 2)))

(deftest cov
  (is (= (m/cov (g/series [1 2 3]) (g/series [3 2 1]) -1.0)))
  (is (= (u/->clj (m/cov (g/data-frame (to-array-2d [[1 2] [3 4] [5 6]]))))
         [{0 4.0, 1 4.0} {0 4.0, 1 4.0}])))

(deftest base-cumulative
  (are [op out]
      (= (vec ((#'m/base-cumulative op) (g/series (range 10)))) out)
    :max (range 10)
    :min (repeat 10 0)
    :prod (repeat 10 0)
    :sum [0 1 3 6 10 15 21 28 36 45])
  (is (= (drop 1 (vec ((#'m/base-cumulative :diff) (g/series (range 10)))))
         (repeat 9 1.0)))
  (is (= ((#'m/base-cumulative :cmpnd) (g/series (range 10)))
         3628799)))
