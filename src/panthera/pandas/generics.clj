(ns panthera.pandas.generics
  "Here is a collection of generic functions and
  methods that help managing the underlying data
  structures such as series and data-frame."
  (:refer-clojure
   :exclude [any?])
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]))

(defn series
  "Creates a panthera series, the underlying backend is a
  pandas Series.

  **Arguments**

  - `data`: basically everything that is Iterable, single values are ok as well

  **Attrs**

  - `:index` -> panthera index, Iterable: if not provided the index gets set to
  a monotonically increasing value (e.g. `(range (count data))`). The provided
  collection must have the same length as data and the values can be anything:
  str, num, keywords
  - `:dtype` -> str, keyword, numpy dtype: coerce the values to the given dtype
  - `:name` -> str or keyword: assign the given name to the series (becomes
  column label when in a data-frame)

  **Examples**

  ```
  (series [1 2 3])

  (series (range 10))

  (series [{:a 1} {:a 2} {:a 3}])

  (series [1 2 3] {:index [:a :b :c]})

  (series [1 2 3] {:name \"my-srs\"})

  (series [1 2 3] {:name \"my-srs\"
                   :index [:a :b :c]})

  (series [\"1.3\" \"3.0\"] {:dtype :float32})
  ```"
  [data & [attrs]]
  (u/kw-call u/pd "Series" data attrs))

(defn data-frame
  "Creates a panthera data-frame, the underlying backend is a
  pandas DataFrame.

  **Arguments**

  - `data`: basically everything that is Iterable and 2D

  **Attrs**

  - `:index` -> panthera index, Iterable: if not provided the index gets set to
  a monotonically increasing value (e.g. `(range (count data))`). The provided
  collection must have the same length as data and the values can be anything:
  str, num, keywords
  - `:columns` -> panthera index, Iterable: labels for the columns (must have
  the same length as the columns)
  - `:dtype` -> str, keyword, numpy dtype: coerce the values to the given dtype,
  only one is allowed

  **Examples**

  ```
  (data-frame [{:a 1 :b 1} {:a 2 :b 2}])

  (data-frame (to-array-2d [[1 2 3] [4 5 6]]))

  (data-frame [(series [1 2 3]) (series [4 5 6])])
  ```"
  [data & [attrs]]
  (u/kw-call u/pd "DataFrame" data attrs))

(defn read-csv
  "Reads the csv from the given path and returns the proper data structure.

  **Arguments**

  - `filename` -> str: a path

  **Attrs**

  - `:sep` -> str, default \",\": the character used as a separator, if longer
  than one (except for \"\\s(+\") gets interpreted as a regex. For more info
  check [original docs](https://pandas.pydata.org/pandas-docs/stable/reference/api/pandas.read_csv.html)
  - `:header` -> int, Iterable of ints: the row number to use as header. By
  default gets inferred as `:header` 0
  - `:names` -> Iterable: list of column names to use as labels. Duplicates are
  not allowed
  - `:index-col` -> int, str, Iterable, false, default nil: column(s) to use as
  the row labels of the data-frame, either given as string name or column index.
  N.B.: `false` can be used to force panthera to not use the first column as
  index
  - `:usecols` -> Iterable: return only the given columns. All elements must
  be either all positional or all label-based.
  - `:squeeze` -> bool, default false: if the csv only contains one column
  then return a series
  - `:prefix` -> str: add a prefix to column numbers when there's no header
  - `:mangle-dupe-cols` -> bool, default true: duplicate column names will be
  deduped, for instance 'X', 'X.1', etc. If false duplicate columns will be
  overwritten
  - `:dtype` -> dtype or map: either a single data type for all data, or a map
  with col-name -> dtype. E.g. {:a :int32 :b :float32}
  - `:engine` -> \"c\" or \"python\": don't touch this unless you know
  exactly what you're doing
  - `:true-values` -> Iterable: values to consider as true
  - `:false-values` -> Iterable: values to consider as false
  - `:skipinitialspace` -> bool, default false: whether to skip spaces after
  the delimiter
  - `:skiprows` -> int, Iterable: number of lines to skip at the beginning of
  the file. If Iterable skip all the given rows (index based)
  - `:skipfooter` -> int: number of lines to skip at the end of the file
  - `:nrows` -> int: number of rows to read at once
  - `:na-values` -> str, Iterable, map: strings to recognize as NaN, if map
  then it can be column based. The following are the default strings recognized
  as NaN: ‘’, ‘#N/A’, ‘#N/A N/A’, ‘#NA’, ‘-1.#IND’, ‘-1.#QNAN’, ‘-NaN’,
  ‘-nan’, ‘1.#IND’, ‘1.#QNAN’, ‘N/A’, ‘NA’, ‘NULL’, ‘NaN’, ‘n/a’, ‘nan’, ‘null’
  - `:keep-default-na` -> bool, default true: include the default NaN flags.
  - `:na-filter` -> bool, default true: detect NaN when reading. If you have a
  large file without missing values this can speed up reading
  - `:verbose` -> bool, default false: print some stats when reading
  - `:skip-blank-lines` -> bool, default true: skip blank lines rather than
  interpreting as missing values
  - `:parse-dates` -> bool, Iterable, map, default false:
      * if true try parsing the index as a date
      * if Iterable of ints/strings then try to parse the corresponding
  columns as date -> [1 2 5] parses columns 1, 2 and 5 
      * if Iterable of Iterables then combine the corresponding columns and
  parse the result as date -> [[1 3]] combine column 1 and 3 and parse the result
      * if map values are an Iterable of columns and keys the name to give to
  the resulting column -> {:date-col [1 3]}
  "
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

(defn not-na?
  [df-or-srs]
  (py/call-attr df-or-srs "notna"))

(defn all?
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "all" attrs))

(defn any?
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "any" attrs))

(defn select-rows
  "This is used for filtering by index.
  
  **Arguments**:

  - `df-or-srs`: a data-frame or a series
  - `idx`: a vector, sequence, series, slice or array of the needed indices
  - `how`: method for subsetting. `:iloc` is purely positional,
  `:loc` is based on labels or booleans. Default is `:iloc`

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
  ;returns all rows which labels are between a and f
  
  (select-rows df [(slice 1 3) (slice)] :loc)
  ; returns rows between 1 and 3 and all columns
  ```"
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

  - `df` -> data-frame
  - `cols` -> str, keyword, num: can be the name of a column (string or value),
  a collection of column names or a collection of values

  **Attrs**:

  - `:drop` -> bool, default true: delete columns set as index.
  **N.B.**: if you need to convert data to Clojure it's
  easier to set this to false and not drop columns
  - `:append` -> bool, default true: add the new index to the
  current one
  - `:verify-integrity` -> bool, default false: check the new
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

  **Arguments**

  - `df-or-srs` -> data-frame or series
  - `i`, `j` -> int, str: the levels that you want to swap

  Examples:

  ```
  (swap-level my-df 0 1)

  (swap-level my-srs :a :b)

  (swap-level my-df 0 :b)
  ```"
  [df-or-srs i j]
  (u/simple-kw-call df-or-srs "swaplevel" [] {"i" i "j" j}))
