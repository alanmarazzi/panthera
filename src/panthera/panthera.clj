(ns panthera.panthera
  (:refer-clojure
   :exclude [mod])
  (:require
   [libpython-clj.python :as py]
   [tech.parallel.utils :refer [export-symbols]]
   [panthera.pandas.generics :as g]
   [panthera.pandas.math :as m]
   [panthera.pandas.utils :as u]))

(export-symbols
 panthera.pandas.generics
 series
 data-frame
 read-csv
 read-excel
 one-hot
 unique
 index
 values
 dtype
 ftype
 shape
 n-rows
 n-cols
 nbytes
 memory-usage
 hasnans?
 series-name
 subset-rows
 cross-section
 head
 subset-cols
 nlargest
 nsmallest
 nunique
 unique?
 monotonic?
 increasing?
 decreasing?
 value-counts)

(export-symbols panthera.pandas.math
 add
 sub
 mul
 div
 mod
 pow
 lt
 gt
 le
 ge
 eq
 ne
 dot
 abs
 all?
 autocorr
 between
 clip
 corr
 cnt
 cov
 cummax
 cummin
 cumprod
 cumsum
 diff
 compound
 kurtosis
 mean-abs-dev
 maximum
 minimum
 mean
 median
 mode
 pct-change
 quantile
 rank
 round
 sem
 skew
 std
 var)

