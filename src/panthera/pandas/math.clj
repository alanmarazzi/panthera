(ns panthera.pandas.math
  (:refer-clojure
   :exclude [any? mod])
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]))

(defn- base-math
  [k]
  (fn [& args]
    (reduce #(py/call-attr %1
                           ({:+    "__add__"
                             :-    "__sub__"
                             :*    "__mul__"
                             :div  "__div__"
                             :fld  "__floordiv__"
                             :mod  "__mod__"
                             :**   "__pow__"
                             :<    "__lt__"
                             :>    "__gt__"
                             :<=   "__le__"
                             :>=   "__ge__"
                             :!=   "__ne__"
                             :=    "__eq__"
                             :dot  "__matmul__"} k)
                           %2) args)))

(defn ops
  [df-or-srs other op & [attrs]]
  (u/kw-call
   df-or-srs
   ({:+    "__add__"
     :-    "__sub__"
     :*    "__mul__"
     :div  "__div__"
     :fld  "__floordiv__"
     :mod  "__mod__"
     :**   "__pow__"
     :<    "__lt__"
     :>    "__gt__"
     :<=   "__le__"
     :>=   "__ge__"
     :!=   "__ne__"
     :=    "__eq__"
     :dot  "__matmul__"} op)
   other
   attrs))

(def add
  (base-math :+))

(def sub
  (base-math :-))

(def mul
  (base-math :*))

(def div
  (base-math :div))

(def mod
  (base-math :mod))

(def pow
  (base-math :**))

(def lt
  (base-math :<))

(def gt
  (base-math :>))

(def le
  (base-math :<=))

(def ge
  (base-math :>=))

(def eq
  (base-math :=))

(def ne
  (base-math :!=))

(def dot
  (base-math :dot))

(defn abs
  [df-or-srs]
  (py/call-attr df-or-srs "abs"))

(defn all?
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "all" attrs))

(defn any?
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "any" attrs))

(defn autocorr
  [srs & [lag]]
  (py/call-attr srs "autocorr" (or lag 1)))

(defn between
  [srs left right & inclusive]
  (py/call-attr srs "between" left right (or inclusive true)))

(defn clip
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "clip" attrs))

(defn corr
  [df-or-srs & args]
  (if (= :data-frame (u/pytype df-or-srs))
    (u/simple-kw-call df-or-srs "corr" (first args))
    (u/kw-call df-or-srs "corr" (first args) (second args))))

(defn cnt
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "count" attrs))

(defn cov
  [df-or-srs & args]
  (if (= :data-frame (u/pytype df-or-srs))
    (u/simple-kw-call df-or-srs "cov" (first args))
    (u/kw-call df-or-srs "cov" (first args) (second args))))

(defn- base-cumulative
  [k]
  (fn [df-or-srs & [attrs]]
    (u/simple-kw-call df-or-srs
                      ({:max   "cummax"
                        :min   "cummin"
                        :prod  "cumprod"
                        :sum   "cumsum"
                        :diff  "diff"
                        :cmpnd "compound"} k)
                      attrs)))

(def cummax
  (base-cumulative :max))

(def cummin
  (base-cumulative :min))

(def cumprod
  (base-cumulative :prod))

(def cumsum
  (base-cumulative :sum))

(def diff
  (base-cumulative :diff))

(def compound
  (base-cumulative :cmpnd))

(defn describe
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "describe" attrs))

(defn- other-ops
  [k]
  (fn [df-or-srs & [attrs]]
    (u/simple-kw-call df-or-srs
                      ({:kurt   "kurtosis"
                        :mad    "mad"
                        :max    "max"
                        :min    "min"
                        :mean   "mean"
                        :median "median"
                        :mode   "mode"
                        :pct    "pct_change"
                        :quant  "quantile"
                        :rank   "rank"
                        :round  "round"
                        :sem    "sem"
                        :skew   "skew"
                        :std    "std"
                        :var    "var"} k)
                      attrs)))

(def kurtosis
  (other-ops :kurt))

(def mean-abs-dev
  (other-ops :mad))

(def maximum
  (other-ops :max))

(def minimum
  (other-ops :min))

(def mean
  (other-ops :mean))

(def median
  (other-ops :median))

(def mode
  (other-ops :mode))

(def pct-change
  (other-ops :pct))

(def quantile
  (other-ops :quant))

(def rank
  (other-ops :rank))

(def round
  (other-ops :round))

(def sem
  (other-ops :sem))

(def skew
  (other-ops :skew))

(def std
  (other-ops :std))

(def var
  (other-ops :var))