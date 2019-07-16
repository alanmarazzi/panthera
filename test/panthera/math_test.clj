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
    :* 1/2 [0.5 1 1.5]
    :* (g/series [3 3 3]) [3 6 9]

    :div 2 [0.5 1 1.5]
    :div 1/2 [2 4 6]
    :div (g/series [2 2 1]) [0.5 1.0 3.0]

    :fld 2 [0 1 1]))
