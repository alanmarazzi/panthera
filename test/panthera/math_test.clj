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
