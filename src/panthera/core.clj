(ns panthera.core
  (:refer-clojure
   :exclude [mod])
  (:require
   [libpython-clj.python :as py]
   [tech.parallel.utils :refer [export-symbols]]
   [panthera.pandas.utils :as u :reload true]))

(export-symbols panthera.pandas.generics
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


(comment
  (defonce np (py/import-module "numpy"))

  (defn melt
    [df & [attrs]]
    (py/call-attr-kw pandas "melt" [df]
                     (u/keys->pyargs attrs)))

  (defn printdf
    [df & [n]]
    (if (< (n-rows df) 50)
      (pp/print-table (py/call-attr df "to_dict" "records"))
      (-> (py/call-attr df "head" (or n 5))
          (py/call-attr "to_dict" "records")
          pp/print-table)))

  (defn printsr
    [sr & [n]]
    (let [s (-> (py/call-attr sr "head" (or n 5))
                (py/call-attr "to_dict"))]
      (println)
      (println (py/get-attr sr "name"))
      (pp/print-table [s])))

  (defmulti printpd (fn [d & _] (py/python-type d)))

  (defmethod printpd :data-frame [d & [n]] (printdf d n))
  (defmethod printpd :series [d & [n]] (printsr d n))

  (comment
    (defmethod print-method :pandas
      [pdobj w]
      (binding [pp/*print-pretty* true]
        (.write ^Writer w ^String (pp/write ^Object pdobj)))))

  (defn row-by-value
    [df cols value & {:keys [copy?]
                      :or   {copy? false}}]
    (let [bools (-> (py/get-item df cols)
                    (py/get-attr "str")
                    (py/call-attr "contains" value))]
      (if copy?
        (py/call-attr
         (py/get-item df bools)
         "copy")
        (py/get-item df bools))))

  (defn sort-df
    [df & {:keys [by
                  ascending
                  axis
                  kind
                  na-position]
           :or   {ascending   true
                  kind        "quicksort"
                  na-position "last"
                  axis        0}}]
    (let [by (flatten (vector by))]
      (py/call-attr-kw df "sort_values"
                       []
                       {"by"          (py/->py-list by)
                        "ascending"   ascending
                        "kind"        kind
                        "na_position" na-position
                        "axis"        axis})))

  (defn dropna
    [df-or-srs & [attrs]]
    (py/call-attr-kw df-or-srs "dropna" [] (u/keys->pyargs attrs)))

  (defn filter-pd
    [df-or-srs colname pred]
    (as-> df-or-srs ds
      (subset-cols ds colname)
      (dropna ds)
      (keep-indexed (fn [id it]
                      (when pred
                        id)) ds)
      (py/get-item
       (py/get-attr df-or-srs "iloc")
       ds)))

  (defn mean
    [df-or-srs]
    (py/call-attr df-or-srs "mean"))

  (defn add-cols
    [df-or-srs cols]
    (py/call-attr-kw df-or-srs "assign" []
                     (u/keys->pyargs cols)))

  (defn tail
    [df-or-srs & [n]]
    (py/call-attr df-or-srs "tail" (or n 5)))

  (defn value-counts
    [df & [attrs]]
    (py/call-attr-kw df "value_counts" [] (u/keys->pyargs attrs)))

  (defn fillna
    [df-or-srs value & [attrs]]
    (py/call-attr-kw df-or-srs "fillna" [value]
                     (u/keys->pyargs attrs)))

  (defn pd-concat
    [df1 df2 & [attrs]]
    (py/call-attr-kw pandas "concat" [[df1 df2]]
                     (u/keys->pyargs attrs))))

(defn concentration
  [df measure]
  (let [ms       (py/get-attr df measure)
        perctot  (-> ms
                     (py/call-attr "cumsum")
                     (py/call-attr "divide" (py/call-attr ms "sum"))
                     (py/call-attr "mul" 100))
        idx      (index df)
        percmeas (-> (py/call-attr idx "div" (n-rows df))
                     (py/call-attr "mul" 100))]
    (py/call-attr-kw (py/call-attr ms "to_frame") "assign" []
                     {(str "perc" measure) perctot
                      "percindex"            percmeas})))

(defn first-20
  [df measure]
  (let [c       (subset-cols df measure)
        lin     (py/get-item c (py/->py-list [0 2 4 9 19]))
        idx     [1 3 5 10 20]
        colname [(str "Share of " measure)]]
    (py/call-attr-kw pandas "DataFrame" []
                     {"data"    (py/get-attr lin "values")
                      "index"   idx
                      "columns" colname})))
