(ns panthera.pandas.reshape
  (:require
    [libpython-clj.python :as py]
    [panthera.pandas.utils :as u]))

(defn crosstab
  "Compute a cross tabulation of two (or more) factors. By default
  computes a frequency table of the factors unless an array of values and an
  aggregation function are passed.

  **Arguments**

  - `seq-or-srs` -> seqable, series

  **Attrs**

  - `:columns` -> Iterable, series, Iterable of Iter/srs: values to group by
  - `:values` -> Iterable, series, Iterable of Iter/srs: values to group
  according to factors, requires `:aggfunc`
  - `:rownames` -> Iterable, series: the names of `seq-or-srs`
  - `:colnames` -> Iterable, series: the names of `:columns`
  - `:aggfunc` -> function, keyword, str: the aggregation function, requires
  `:values`. It can be a panthera function (pt/sum), a numpy function (npy :sum),
  the name of a numpy function (:mean or \"mean\") or a Clojure function. In the
  latter case be aware that you have to reduce over a map.
  - `:margins` -> bool, default false: add subtotals
  - `:margins_name`: str, default \"All\": name of the row/column holding totals
  when `:margins` true
  - `:dropna` -> bool, default true: exclude columns with all missing values
  - `:normalize` -> bool, {\"all\" \"index\" \"columns\"}, {0 1}, default false:
  normalize by dividing all values by the sum of values

  **Examples**

  ```
  (crosstab [[1 2 2]] {:columns [[:a :b :a]]})
  ;; col_0  a  b
  ;; row_0      
  ;; 1      1  0
  ;; 2      1  1

  (crosstab [[1 2 2]] {:columns [[:a :b :a]]
                       :rownames [:myrows]
                       :colnames [:mycols]})
  ;; mycols  a  b
  ;; myrows      
  ;; 1       1  0
  ;; 2       1  1

  (crosstab [[1 2 2]] {:columns [[:a :b :b]]
                       :values [10 20 30]
                       :aggfunc :mean})
  ;; col_0     a     b
  ;; row_0            
  ;; 1      10.0   NaN
  ;; 2       NaN  25.0

  (crosstab [[1 2 2]] {:columns [[:a :b :a]]
                       :margins true})
  ;; col_0  a  b  All
  ;; row_0           
  ;; 1      1  0    1
  ;; 2      1  1    2
  ;; All    2  1    3
  ```
  "
  [seq-or-srs & [attrs]]
  (u/kw-call u/pd "crosstab" seq-or-srs attrs))

(defn pivot
  [df & [attrs]]
  (u/simple-kw-call df "pivot" attrs))

(defn cut
  [data-or-srs bins & [attrs]]
  (py/call-attr-kw u/pd "cut" [data-or-srs bins]
                   (u/keys->pyargs attrs)))

(defn qcut
  [data-or-srs q & [attrs]]
  (py/call-attr-kw u/pd "qcut" [data-or-srs q]
                   (u/keys->pyargs attrs)))

(defn merge-ordered
  [left right & [attrs]]
  (py/call-attr-kw u/pd "merge_ordered" [left right]
                   (u/keys->pyargs attrs)))

(defn merge-asof
  [left right & [attrs]]
  (py/call-attr-kw u/pd "merge_asof" [left right]
                   (u/keys->pyargs attrs)))

(defn concatenate
  [dfs-or-srss & [attrs]]
  (u/kw-call u/pd "concat" dfs-or-srss attrs))

(defn factorize
  [seq-or-srs & [attrs]]
  (u/kw-call u/pd "factorize" seq-or-srs attrs))

(defn aggregate
  [df-or-srs how & [attrs]]
  (u/kw-call df-or-srs "agg" how attrs))

(defn remap
  [df-or-srs mappings & [na-action]]
  (py/call-attr df-or-srs "map" mappings (or na-action nil)))

(defn groupby
  [df-or-srs by & [attrs]]
  (u/kw-call df-or-srs "groupby" by attrs))

(defn rolling
  [df-or-srs window & [attrs]]
  (u/kw-call df-or-srs "rolling" window attrs))

(defn ewm
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "ewm" attrs))

; remove :inplace as an attr
(defn dropna
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "dropna" attrs))

(defn melt
  [df & [attrs]]
  (u/simple-kw-call df "melt" attrs))

(defn assign
  [df-or-srs cols]
  (u/simple-kw-call df-or-srs "assign"
                    (u/keys->pyargs cols)))

(defn unstack
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "unstack" attrs))

(defn transpose
  "Transpose the given panthera object"
  [df-or-srs]
  (py/call-attr df-or-srs "transpose"))
