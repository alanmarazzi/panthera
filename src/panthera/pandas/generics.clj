(ns panthera.pandas.generics
  "Here is a collection of generic functions and
  methods that help managing the underlying data
  structures such as series and data-frame."
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]))

(defn series
  "Creates a panthera series, the underlying backend is a
  pandas Series."
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
  [df-or-srs]
  (py/get-attr df-or-srs "index"))

(defn values
  [df-or-srs]
  (py/get-attr df-or-srs "values"))

(defn dtype
  [df-or-srs]
  (py/get-attr df-or-srs "dtypes"))

(defn ftype
  [srs]
  (py/get-attr srs "ftypes"))

(defn shape
  "Returns the shape of the given object. If a
  [[data-frame]] the first value is the count of rows
  and the second one the count of columns. If a
  [[series]] there are no columns.

  ```
  (shape df)
  ;; [800 12]

  (shape sr)
  ;; 800
  ```"
  [df-or-srs]
  (py/get-attr df-or-srs "shape"))

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
  [srs]
  (py/get-attr srs "hasnans"))

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

(defn n-largest
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "nlargest" attrs))

(defn n-smallest
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "nsmallest" attrs))

(defn n-unique
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "nunique" attrs))

(defn- preds
  [k]
  (fn [seq-or-srs]
    (let [ks {:unique?     #(py/get-attr % "is_unique")
              :increasing? #(py/get-attr % "is_monotonic_increasing")
              :decreasing? #(py/get-attr % "is_monotonic_decreasing")}]
      (if (u/series? seq-or-srs)
        ((ks k) seq-or-srs)
        (recur (series seq-or-srs))))))

(def unique?
  (preds :unique?))

(def increasing?
  (preds :increasing?))

(def decreasing?
  (preds :decreasing?))

(defn value-counts
  [seq-or-srs & [attrs]]
  (if (u/series? seq-or-srs)
    (let [v (u/simple-kw-call
             seq-or-srs
             "value_counts"
             (dissoc attrs :clj))]
      (if (:clj attrs)
        (zipmap 
         (map u/memo-columns-converter (vec (index v))) 
         (vec v))
        v))
    (recur (series seq-or-srs) [attrs])))

(defn to-csv
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "to_csv" attrs))

(defn reset-index
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "reset_index" attrs))

(defn names
  [df-or-srs]
  (if (u/series? df-or-srs)
    (py/get-attr df-or-srs "name")
    (py/get-attr df-or-srs "columns")))

(defn filter-rows
  [df-or-srs bools-or-func]
  (if (fn? bools-or-func)
    (py/get-item df-or-srs (u/vals->pylist (bools-or-func df-or-srs)))
    (py/get-item df-or-srs (u/vals->pylist bools-or-func))))

(defn tail
  [df-or-series & [n]]
  (py/call-attr df-or-series "tail" (or n 5)))

(defn fill-na
  [df-or-srs value & [attrs]]
  (u/kw-call df-or-srs "fillna" value attrs))

; Watch out!!!!! Use these 2 only confined in Python land
(defn not-na?
  [df-or-srs]
  (py/call-attr df-or-srs "notna"))

(defn all?
  [df-or-srs]
  (py/call-attr df-or-srs "all"))

(defn select-rows
  "This is used for filtering by index.
  
  **Arguments**:

  `df-or-srs`: a data-frame or a series
  `idx`: a vector, sequence, series, slice 
  or array of the needed indices
  `how`: method for subsetting. `:iloc` is
  purely positional, `:loc` is based on labels
  or booleans. Default is `:iloc`

  Examples:

  ```
  (select-rows df [1 3])
  ; returns the second and fourth row

  (select-rows srs [3 7])
  ; returns the fourth and eight row
  
  (select-rows df [\"a\" \"b \"] :loc)
  ; returns the rows labeled a and b

  (select-rows df (slice 5))
  ; returns the first 5 rows

  (select-rows df (slice \"a \" \"f \"))
  ;returns all rows which labels are between a and f"
  [df-or-srs idx & [how]]
  (case how
    :iloc (-> (py/get-attr df-or-srs "iloc")
              (py/get-item (u/vals->pylist idx)))
    :loc  (-> (py/get-attr df-or-srs "loc")
              (py/get-item (u/vals->pylist idx)))
    (-> (py/get-attr df-or-srs "iloc")
        (py/get-item (u/vals->pylist idx)))))

(defn set-index
  "Set the index of the given data-frame,
  this can be either a column, multiple columns or
  a series-like collection.

  **Arguments**:

  `df`: a data-frame

  `cols`: can be the name of a column (string or value),
  a collection of column names or a collection of values

  **Attrs**:

  `:drop` -> bool, default true: delete columns set as index.
  **N.B.**: if you need to convert data to Clojure it's
  easier to set this to false and not drop columns

  `:append` -> bool, default true: add the new index to the
  current one

  `:verify-integrity` -> bool, default false: check the new
  index for duplicates

  Examples:

  ```
  (set-index my-df \"mycol\")
  ; this drops the current index and replaces it with mycol

  (set-index my-df \"mycol\" {:drop false})
  ; this sets mycol as the new index and keeps mycol in the data-frame

  (set-index my-df [12 24])

  (set-index my-df (series [:a :b]))
  ```"
  [df cols & [attrs]]
  (u/kw-call df "set_index" cols attrs))

(defn swap-level
  "Switch levels `i` and `j` of a multi index.

  Arguments:

  `df-or-srs`: data-frame or series
  `i`, `j` -> int, str: the levels that you want to swap

  Examples:

  ```
  (swap-level my-df 0 1)

  (swap-level my-srs :a :b)

  (swap-level my-df 0 :b)
  ```"
  [df-or-srs i j]
  (u/simple-kw-call df-or-srs "swaplevel" [] {"i" i "j" j}))
