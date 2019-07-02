(ns panthera.pandas.generics
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]))

(defn series
  [data & [attrs]]
  (u/kw-call u/pd "Series" data attrs))

(defn data-frame
  [data & [attrs]]
  (u/kw-call u/pd "DataFrame" data attrs))

(defn read-csv
  [filename & [attrs]]
  (u/kw-call u/pd "read_csv" filename attrs))

(defn read-excel
  [filename & [attrs]]
  (u/kw-call u/pd "read_excel" filename attrs))

; get_dummies
(defn one-hot
  [df-or-srs & [attrs]]
  (u/kw-call u/pd "get_dummies" df-or-srs attrs))

(defn unique
  [seq-or-srs]
  (py/call-attr u/pd "unique" seq-or-srs))

;;; wide_to_long not implemented, overlaps with melt

(defn index
  [df-or-srs & clj?]
  (if clj?
    (vec (py/get-attr df-or-srs "index"))
    (py/get-attr df-or-srs "index")))

(defn values
  [df-or-srs & clj?]
  (if clj?
    (vec (py/get-attr df-or-srs "values"))
    (py/get-attr df-or-srs "values")))

(defn dtype
  [df-or-srs]
  (py/get-attr df-or-srs "dtypes"))

(defn ftype
  [srs]
  (py/get-attr srs "ftypes"))

(defn shape
  "Returns the shape of the given object. If a
  [[dataframe]] the first value is the count of rows
  and the second one the count of columns. If a
  [[series]] there are no columns.

  ```
  (shape df)
  ;; [800 12]

  (shape sr)
  ;; 800
  ```"
  [df-or-srs & clj?]
  (if clj?
    (vec (py/get-attr df-or-srs "shape"))
    (py/get-attr df-or-srs "shape")))

(defn n-rows
  "Returns the number of rows for the given object."
  [df-or-srs]
  ((shape df-or-srs) 0))

(defn n-cols
  "Returns the number of columns for the given object."
  [df]
  ((shape df) 1))

(defn nbytes
  [srs]
  (py/get-attr srs "nbytes"))

;; ndim & size not implemented, they are the shape values. strides is deprecated

(defn memory-usage
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "memory_usage" attrs))

(defn hasnans?
  "This is a cached value, but by never mutating the underlying
  data we get very nice speed improvements"
  [df-or-srs]
  (py/get-attr df-or-srs "hasnans"))

(defn series-name
  [srs]
  (py/get-attr srs "name"))

(defn subset-rows
  "Select rows by index"
  [df & slicing]
  (py/get-item
   (py/get-attr df "iloc")
   (apply u/slice slicing)))

(defn cross-section
  [df-or-srs k & [attrs]]
  (u/kw-call df-or-srs "xs" k attrs))

(defn head
  [df-or-srs & [n]]
  (py/call-attr df-or-srs "head" (or n 5)))

(defn subset-cols
  "Select columns by name"
  [df & colnames]
  (let [cols (if (= 1 (count colnames))
               (first colnames)
               (apply py/->py-list [colnames]))]
    (py/get-item df cols)))

(defn nlargest
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "nlargest" attrs))

(defn nsmallest
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "nsmallest" attrs))

(def nunique
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "nunique" attrs))

(defn preds
  [k]
  (fn [seq-or-srs]
    (let [ks {:unique?     #(py/get-attr % "is_unique")
              :monotonic?  #(py/get-attr % "is_monotonic")
              :increasing? #(py/get-attr % "is_monotonic_increasing")
              :decreasing? #(py/get-attr % "is_monotonic_decreasing")}]
      (if (u/series? seq-or-srs)
        ((ks k) seq-or-srs)
        (recur (series seq-or-srs))))))

(def unique?
  (preds :unique?))

(def monotonic?
  (preds :monotonic?))

(def increasing?
  (preds :increasing?))

(def decreasing?
  (preds :decreasing?))

(defn value-counts
  [seq-or-srs & [attrs]]
  (if (u/series? seq-or-srs)
    (u/simple-kw-call seq-or-srs "value_counts" attrs)))
