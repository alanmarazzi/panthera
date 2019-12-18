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
  - `:infer-datetime-format` -> bool, default false: if true and `:parse-dates`
  is enabled this will try to infer the datetime format of the strings
  - `:keep-date-col` -> bool, default false: if true and `:parse-dates` combines
  columns this keeps the original date column around
  - `:dayfirst` -> bool, default false: international date formatting (DD/MM)
  - `:cache-dates` -> bool, default true: use a cache of unique dates to speed
  up the conversion of duplicate dates
  - `:iterator` -> bool, default false: returns a 'generator' of the file,
  use `:chunksize` instead
  - `:chunksize` -> int: read the file in chunks. The result is a lazy iterator
  - `:compression` -> {\"infer\" \"gzip\" \"bz2\" \"zip\" \"xz\" nil}, default \"infer\":
  decompress on the fly
  - `:thousands` -> str: thousands separator
  - `:decimal` -> str, default \".\": character to use as decimal separator
  - `:lineterminator` -> str: character to break lines. Works only with the C
  parser
  - `:quotechar` -> str: character used to denote the start and end of a quoted
  item. Quoted items can include the delimiter and it will be ignored
  - `:quoting` -> int, default 0: quoting constants. QUOTE_MINIMAL (0),
  QUOTE_ALL (1), QUOTE_NONNUMERIC (2) or QUOTE_NONE (3)
  - `:doublequote` -> bool, default true: when `:quotechar` is specified and
  `:quoting` is not QUOTE_NONE, indicate whether or not to interpret two
  consecutive quotechar elements inside a field as a single quotechar element
  - `:escapechar` -> str: the character used for escaping
  - `:comment` -> str: character used for comments
  - `:encoding` -> str: for instance \"utf-8\"
  - `:dialect` -> {\"excel\" \"excel-tab\" \"unix\"}: specify the dialect and
  avoid setting all of `:delimiter`, `:doublequote`, `:escapechar`, `:skipinitialspace`,
  `:quotechar`, and `:quoting`
  - `:error-bad-lines` -> bool, default true: lines with too many fields cause
  an exception, if false drop them
  - `:warn-bad-lines` -> bool, default true: warn when dropping bad lines
  - `:delim-whitespace` -> bool, default false: i whether or not whitespace will
  be used as a separator
  - `:low-memory` -> bool, default true: this makes internal processing chunked,
  anyway the file is read all at once, this impacts only memory while reading.
  This can cause mixed type inference, if you have issues either set this to
  false or specify `:dtype`
  - `:memory-map` -> bool, default false: map the file directly onto memory and
  access data from there
  - `:float-precision` -> str: specifies the converter used by the C engine

  **Examples**

  ```
  (read-csv \"mycsv.csv\")

  (read-csv \"mycsv.csv\" {:sep \";\"})
  ```"
  [filename & [attrs]]
  (u/kw-call u/pd "read_csv" filename attrs))

(defn read-excel
  "Reads an Excel file into a data-frame. This requires one Excel reader
  (e.g. xlrd) to be installed in your environment.

  **Arguments**

  - `filename` -> str: a path

  **Attrs**

  - `:sheet-name` -> keyword, str, int, Iterable: keyword, str and Iterable of
  them are used to refer to sheet labels, int is positional (starting from 0).
  If given multiple sheets returns a map of data-frames
  - `:header` -> int, Iterable of ints: the row number to use as header. By
  default gets inferred as `:header` 0
  - `:names` -> Iterable: list of column names to use as labels. Duplicates are
  not allowed
  - `:index-col` -> int, str, Iterable, false, default nil: column(s) to use as
  the row labels of the data-frame, either given as string name or column index.
  N.B.: `false` can be used to force panthera to not use the first column as
  index
  - `:usecols` -> Iterable: return only the given columns. All elements must
  be either all positional or all label-based
  - `:squeeze` -> bool, default false: if the csv only contains one column
  then return a series
  - `:dtype` -> dtype or map: either a single data type for all data, or a map
  with col-name -> dtype. E.g. {:a :int32 :b :float32}
  - `:engine` -> \"c\" or \"python\": don't touch this unless you know
  exactly what you're doing
   - `:true-values` -> Iterable: values to consider as true
  - `:false-values` -> Iterable: values to consider as false
  - `:skiprows` -> int, Iterable: number of lines to skip at the beginning of
  the file. If Iterable skip all the given rows (index based)
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
  - `:thousands` -> str: thousands separator
  - `:comment` -> str: character used for comments
  - `:skipfooter` -> int: number of lines to skip at the end of the file
  - `:mangle-dupe-cols` -> bool, default true: duplicate column names will be
  deduped, for instance 'X', 'X.1', etc. If false duplicate columns will be
  overwritten
  - `:convert-float` -> bool, default true: convert integral floats to int
  (e.g. 1.0 -> 1). If false every number gets parsed as float

  **Examples**

  ```
  (read-excel \"myfile.xlsx\")

  (read-excel \"myfile.xlsx\" {:thousands \".\"})
  ```"
  [filename & [attrs]]
  (u/kw-call u/pd "read_excel" filename attrs))

; get_dummies
(defn one-hot
  "Convert a categorical variable into one-hot encoded columns.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Attrs**

  - `:prefix` -> str, Iterable, map: string to append column names to.
  - `:prefix-sep` -> str, default \"_\": the separator to use when appending to
  `:prefix`
  - `:dummy-na` -> bool, default false: if true add a column for NaNs
  - `:columns` -> Iterable: column names to be encoded. If not specified all
  columns with *object* or *category* dtype will be converted
  - `:sparse` -> bool, default false: whether the encoded columns should be
  backed by a sparse array
  - `:drop-first` -> bool, default false: get k - 1 columns out of k vars
  - `:dtype` -> dtype: the dtype for the new columns, e.g. :int8

  **Examples**

  ```
  (-> (series [:a :b :a]) one-hot ->clj)
  ; [{:a 1 :b 0} {:a 0 :b 1} {:a 1 :b 0}]

  (-> (series [:a :b :a]) (one-hot {:prefix \"my\"}) ->clj)
  ; [{:my-a 1 :my-b 0} {:my-a 0 :my-b 1} {:my-a 1 :my-b 0}]
  ```"
  [df-or-srs & [attrs]]
  (u/kw-call u/pd "get_dummies" df-or-srs attrs))

(defn unique
  "Return unique values in the given coll or srs in order of appearance. It does
  NOT sort.

  **Arguments**

  - `seq-or-srs` -> seqable or series

  **Examples**

  ```
  (unique [1 1 2]) ; [1 2]
  ```"
  [seq-or-srs]
  (py/call-attr u/pd "unique" seq-or-srs))

;;; wide_to_long not implemented, overlaps with melt

(defn index
  "Return the index of the given data-frame or series. The object you get back
  can be used as it is for slicing, indexing and in other functions. If you need
  to use it in Clojure it acts almost as `range`, so you can `vec` convert it
  for instance, or `map` a function over it as it is.

  **Arguments**

  - `df-or-srs` -> data-frame, srs

  **Examples**

  ```
  (index (series [1 2 3]))
  ; RangeIndex(start=0, stop=3, step=1)

  (vec (index (series [1 2 3])))
  ; [0 1 2]
  ```"
  [df-or-srs]
  (py/get-attr df-or-srs "index"))

(defn values
  "Return the underlying \"pure\" data representation.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Examples**

  ```
  (values (data-frame [{:a 1 :b 2} {:a 3 :b 4}]))
  ; [[1 2] [3 4]]
  ```"
  [df-or-srs]
  (py/get-attr df-or-srs "values"))

(defn dtype
  "Return data type of the given object. If a series you get the dtype of the
  data in the series, if data-frame you get a series where every record is the
  dtype of a column.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Examples**

  ```
  (dtype (series [:a :b]))
  ; object

  (dtype (data-frame [{:a 1 :b 2} {:a 3 :b 4}]))
  ;a    int64
  ;b    int64
  ;dtype: object
  ```"
  [df-or-srs]
  (py/get-attr df-or-srs "dtypes"))

(defn ftype
  ^{:deprecated "pandas 0.25.0"
    :doc "Deprecated by pandas 0.25.0, use `dtype` instead"}
  [srs]
  (dtype srs))

(defn shape
  "Returns the shape of the given object. If a
  [[data-frame]] the first value is the count of rows
  and the second one the count of columns. If a
  [[series]] there are no columns.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Examples**

  ```
  (shape df)
  ;; [800 12]

  (shape sr)
  ;; 800
  ```"
  [df-or-srs]
  (py/get-attr df-or-srs "shape"))

(defn n-rows
  "Returns the number of rows for the given object.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Examples**

  ```
  (n-rows df)
  ; 4
  ```"
  [df-or-srs]
  ((shape df-or-srs) 0))

(defn n-cols
  "Returns the number of columns for the given object.

  **Arguments**

  - `df` -> data-frame

  **Examples**

  ```
  (n-cols df)
  ; 5
  ```"
  [df]
  ((shape df) 1))

(defn nbytes
  "Return the number of bytes in the underlying data

  **Arguments**

  - `srs` -> series

  **Examples**

  ```
  (nbytes (series [1 2 3]))
  ; 24
  ```"
  [srs]
  (py/get-attr srs "nbytes"))

;; ndim & size not implemented, they are the shape values. strides is deprecated

(defn memory-usage
  "Return the memory usage of the given object in bytes. If data-frame you get a
  series with a record for every column

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Attrs**

  - `:index` -> bool, default true: include the memory usage of the index
  - `:deep` -> bool, default false: deeper inspection of data types

  **Examples**

  ```
  (memory-usage (data-frame [{:a 1 :b 2} {:a 3 :b 4}]))
  ;Index    80
  ;a        16
  ;b        16
  ;dtype: int64

  (memory-usage (data-frame [{:a 1 :b 2} {:a 3 :b 4}]) {:index false})
  ;a        16
  ;b        16
  ;dtype: int64
  ```"
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "memory_usage" attrs))

(defn hasnans?
  "Check if the given series includes missing values. This is a cached value,
  but by never mutating the underlying data we get speed improvements

  **Arguments**

  - `srs` -> series

  **Examples**

  ```
  (hasnans? (series [1 nil]))
  ; true

  (hasnans? (series [1 3]))
  ; false
  ```"
  [srs]
  (py/get-attr srs "hasnans"))

(defn subset-rows
  "Select rows by index

  **Arguments**

  - `df-or-srs` -> data-frame, series
  - `slicing` -> int: if one int is given then get all values up to that one
  excluded, if 2 are given then they become start and stop, if 3 then the last
  one is the step. If none then return everything. It acts like `range`

  **Examples**

  ```
  (subset-rows (series (range 3)))
  ;0    0
  ;1    1
  ;2    2
  ;dtype: int64

  (subset-rows (series (range 10)) 2)
  ;0    0
  ;1    1
  ;dtype: int64

  (subset-rows (series (range 10)) 2 5)
  ;2    2
  ;3    3
  ;4    4
  ;dtype: int64

  (subset-rows (series (range 10)) 2 6 2)
  ;2    2
  ;4    4
  ;dtype: int64
  ```"
  [df-or-srs & slicing]
  (py/get-item
   (py/get-attr df-or-srs "iloc")
   (apply u/slice slicing)))

(defn cross-section
  "Return a cross-section from the data-frame or series

  **Arguments**
  - `df-or-srs` -> data-frame, series
  - `k` -> keyword, str, int: the label of interest

  **Attrs**

  - `:axis` -> {0 1 \"index\" \"columns\"}, default 0: 0 = \"index\" = row-wise,
  1 = \"columns\" = column-wise
  - `:level` -> str, int, Iterable: in case of a key partially contained in
  a *MultiIndex*, indicate which levels are used. Levels can be referred by
  label or position
  - `:drop-level` -> bool, default true: if false doesn't drop levels

  **Examples**

  ```
  (-> (data-frame [{:a :a :b 1} {:a :b :b 2} {:a :a :b 3}])
      (set-index :a)
      (cross-section :a))
  ;   b
  ;a   
  ;a  1
  ;a  3
  ```"
  [df-or-srs k & [attrs]]
  (u/kw-call df-or-srs "xs" k attrs))

(defn head
  "Return the first n rows

  **Arguments**

  - `df-or-srs` -> data-frame, series
  - `n` -> int, default 5: the number of rows to show

  **Examples**

  ```
  (head (series (range 100)))
  ;0    0
  ;1    1
  ;2    2
  ;3    3
  ;4    4
  ;dtype: int64

  (head (series (range 100)) 2)
  ;0    0
  ;1    1
  ;dtype: int64
  ```"
  [df-or-srs & [n]]
  (py/call-attr df-or-srs "head" (or n 5)))

(defn subset-cols
  "Select columns by name.

  Note that `colnames` are treated as literals: there's no
  conversion involved, so if you pass a keyword it gets
  translated as a string exactly as it is.

  **Arguments**

  - `df` -> data-frame
  - `colnames` -> str, keyword: column labels of interest

  **Examples**

  ```
  (def df (data-frame (map #(zipmap [:a :b :c] %) (partition 3 (range 9)))))

  (subset-cols df :a)
  ;   a
  ;0  0
  ;1  3
  ;2  6

  (subset-cols df \"a\" :c)
  ;   a  c
  ;0  0  2
  ;1  3  5
  ;2  6  8
  ```"
  [df & colnames]
  (let [cols (if (= 1 (count colnames))
               (first colnames)
               (apply py/->py-list [colnames]))]
    (py/get-item df cols)))

(defn n-largest
  "Return the first `:n` values ordered by `:columns` in descending order.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Attrs**

  - `:n` -> int, (for series default 5): number of values/rows to return
  - `:columns` -> str, keyword, Iterable: only for data-frames, labels of the
  columns to order by
  - `:keep` -> {\"first\" \"last\" \"all\"}, default \"first\": what to do with duplicate
  values:
      * *first* -> prioritize first occurrences
      * *last* -> prioritize last occurrences
      * *all* -> don't drop duplicates, potentially return more than `:n`

  **Examples**

  ```
  (def df (data-frame (map #(zipmap [:a :b :c] %) (partition 3 (range 9)))))

  (n-largest df {:n 2 :columns :a})
  ;   a  b  c
  ;2  6  7  8
  ;1  3  4  5

  (n-largest (series (range 10)))
  ;9    9
  ;8    8
  ;7    7
  ;6    6
  ;5    5
  ;dtype: int64
  ```"
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "nlargest" attrs))

(defn n-smallest
  "Return the `:n` smallest elements. It's the same and the
  opposite of [[n-largest]].

  **Attrs**

  - `:n` -> int, (for series default 5): number of values/rows to return
  - `:columns` -> str, keyword, Iterable: only for data-frames, labels of the
  columns to order by
  - `:keep` -> {\"first\" \"last\" \"all\"}, default \"first\": what to do with duplicate
  values:
      * *first* -> prioritize first occurrences
      * *last* -> prioritize last occurrences
      * *all* -> don't drop duplicates, potentially return more than `:n`

  **Examples**

  ```
  (def df (data-frame (map #(zipmap [:a :b :c] %) (partition 3 (range 9)))))

  (n-smallest df {:n 2 :columns :a})
  ;   a  b  c
  ;0  0  1  2
  ;1  3  4  5

  (n-smallest (series (range 10)))
  ;0    0
  ;1    1
  ;2    2
  ;3    3
  ;4    4
  ;dtype: int64
  ```"
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "nsmallest" attrs))

(defn n-unique
  "Count distinct values either in a series or over an axis.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Attrs**

  - `:axis` -> {0 1 \"index\" \"columns\"}, default 0: 0 = \"index\" = row-wise,
  1 = \"columns\" = column-wise
  - `:dropna` -> bool, default true: don't include missing values

  **Examples**

  ```
  (n-unique (series [4 4 5]))
  ;2

  (def df (data-frame (map #(zipmap [:a :b :c] %) (partition 3 (range 9)))))

  (n-unique df)
  ;a    3
  ;b    3
  ;c    3
  ;dtype: int64

  (n-unique df {:axis 1})
  ;0    3
  ;1    3
  ;2    3
  ;dtype: int64
  ```"
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "nunique" attrs))

(defn- preds
  "Dispatcher to avoid repetition"
  [k]
  (fn [seq-or-srs]
    (let [ks {:unique?     #(py/get-attr % "is_unique")
              :increasing? #(py/get-attr % "is_monotonic_increasing")
              :decreasing? #(py/get-attr % "is_monotonic_decreasing")}]
      (if (u/series? seq-or-srs)
        ((ks k) seq-or-srs)
        (recur (series seq-or-srs))))))

(def unique?
  "Return wether the values in the given collection are unique.

  **Arguments**

  - `seq-or-srs` -> seqable or series

  **Examples**

  ```
  (unique? [1 2 3])
  ;true

  (unique? (series [1 1 2]))
  ;false
  ```"
  (preds :unique?))

(def increasing?
  "Equivalent to Clojure's `<`"
  (preds :increasing?))

(def decreasing?
  "Equivalent to Clojure's `>`"
  (preds :decreasing?))

(defn value-counts
  "Return the count of all unique values

  **Arguments**

  - `seq-or-srs` -> seqable, series

  **Attrs**

  - `:normalize` -> bool, default true: return relative frequencies instead of counts
  - `:sort` -> bool, default true: sort by frequencies
  - `:ascending` -> bool, default false: sort order
  - `:bins` -> int: group numeric data into bins of `:bins` size
  - `:dropna` -> bool, default true: don't include missing values

  **Examples**

  ```
  (value-counts (series [2 2 2 4]))
  ;2    3
  ;4    1
  ;dtype: int64

  (value-counts (series [2 2 2 4]) {:normalize true})
  ;2    0.75
  ;4    0.25
  ;dtype: float64
  ```"
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
  "Writes the csv at the given path.

  **Arguments**

  - `df-or-srs` -> data-frame, series
  - `filename` -> str: a path

  **Attrs**

  - `:sep` -> str, default \",\": the character used as a separator
  - `:na-rep` -> str, default \"\": missing data representation
  - `:float-format` -> str: format string for floating point numbers
  - `:columns` -> Iterable: list of columns to write
  - `:header` -> bool, Iterable, default true: if bool decide whether writing
  column names, if Iterable use the content for column names
  - `:index` -> bool, default true: write the index
  - `:index-label` -> str, Iterable, false: column label to use as index column
  - `:mode` -> str: Python write mode, don't touch this unless you know
  what you're doing
  - `:encoding` -> str: for instance \"utf-8\"
  - `:compression` -> {\"infer\" \"gzip\" \"bz2\" zip\" \"xz\"}, default \"infer\":
  if \"infer\" detect compression from the file extension
  - `:quoting` -> int, default 0: quoting constants. QUOTE_MINIMAL (0),
  QUOTE_ALL (1), QUOTE_NONNUMERIC (2) or QUOTE_NONE (3)
  - `:quotechar` -> str, default \": character used to quote
  - `:line-terminator` -> str: the newline character to use
  - `:chunksize` -> int: rows to write in chunks
  - `:date-format` -> format string for dates
  - `:doublequote` -> bool, default true: control quoting of `:quotechar` inside
  a field
  - `:escapechar` -> str: character to escape `:sep` and `:quotechar`
  - `:decimal` -> str, default \".\": character to use as decimal separator

  **Examples**

  ```
  (to-csv mydf \"mycsv.csv\")

  (to-csv \"mycsv.csv\" {:sep \";\" :index false})
  ```"
  [df-or-srs filename & [attrs]]
  (u/simple-kw-call df-or-srs "to_csv" attrs))

(defn reset-index
  "Reset the index or part of it. This replaces the current index
  with the default one.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Attrs**

  - `:level` -> int, str, Iterable: remove the given levels only. Removes all
  by default
  - `:drop` -> bool, default false: if true the old index won't be inserted as a
  column
  - `:col-level` -> int, str, default 0: if there are multiple levels decide which
  level will be replaced by the new index
  - `:col-fill` -> str, default \"\": if there are multiple levels decide how
  the others are named

  **Examples**

  ```
  (reset-index df)

  (reset-index srs)
  ```"
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "reset_index" attrs))

(defn names
  "Get the either the name of the given series or columns names.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Examples**

  ```
  (names (series 1 {:name :my-name}))
  ; \"my-name\"

  (names (data-frame [{:a 1 :b 2}]))
  ; Index(['a', 'b'], dtype='object')
  ```"
  [df-or-srs]
  (if (u/series? df-or-srs)
    (py/get-attr df-or-srs "name")
    (py/get-attr df-or-srs "columns")))

(defn filter-rows
  "Filter rows or rows based on booleans. If a function is given this gets
  evaluated first then the resulting bool array used for filtering.

  **Arguments**

  - `df-or-srs` -> data-frame, series
  - `bools-or-func` -> Iterable, function: either an Iterable of bools or a
  panthera function that takes `df-or-srs` as an input and returns an Iterable
  of bools

  **Examples**

  ```
  (filter-rows (series (range 4)) [true false true false])
  ;0    0
  ;2    2
  ;dtype: int64

  (filter-rows (series (range 4)) #(-> (mod % 2) (eq 0))
  ;0    0
  ;2    2
  ;dtype: int64
  ```"
  [df-or-srs bools-or-func]
  (if (fn? bools-or-func)
    (py/get-item df-or-srs (u/vals->pylist (bools-or-func df-or-srs)))
    (py/get-item df-or-srs (u/vals->pylist bools-or-func))))

(defn tail
  "The opposite of [[head]], returns the last `n` observations

  **Arguments**

  - `df-or-srs` -> data-frame, series
  - `n` -> int, default 5: the number of observations to return

  **Examples**

  ```
  (tail (series (range 10)))
  ;5    5
  ;6    6
  ;7    7
  ;8    8
  ;9    9
  ;dtype: int64

  (tail (series (range 10)) 2)
  ;8    8
  ;9    9
  ;dtype: int64

  (def df (data-frame (map #(zipmap [:a :b :c] %) (partition 2 (range 20)))))

  (tail df)
  ;    a   b
  ;5  10  11
  ;6  12  13
  ;7  14  15
  ;8  16  17
  ;9  18  19

  (tail df 2)
  ;    a   b
  ;8  16  17
  ;9  18  19
  ```"
  [df-or-series & [n]]
  (py/call-attr df-or-series "tail" (or n 5)))

(defn fill-na
  "Fill missing observations with the given `value`

  **Arguments**

  - `df-or-srs` -> data-frame, series
  - `value` -> value, series, data-frame, map, nil: if single value it will be
  used to fill every missing observation, if data-frame, series or map fill value
  by index for series and by column for data-frame/map. If using `:method` pass `nil`

  **Attrs**

  - `:method` -> {\"bfill\" \"ffill\"}: method used for filling, bfill = backfill
  and ffill = forwardfill
  - `:axis` -> {0 1 \"index\" \"columns\"}, default 0: 0 = \"index\" = row-wise,
  1 = \"columns\" = column-wise
  - `:limit` -> int: if `:method` is specified this is the max number of
  consecutive values to fill. If `:method` is not specified this is the max
  number of entries along given `:axis` that will be filled
  - `:downcast` -> map, \"infer\": a map of item to dtype indicating what to
  downcast if possible. \"infer\" tries by itself

  **Examples**

  ```
  (fill-na (series [1 nil 3]) 2)
  ;0    1.0
  ;1    2.0
  ;2    3.0
  ;dtype: float64

  (fill-na (series [1 nil 3]) nil {:method \"bfill\"})
  ;0    1.0
  ;1    3.0
  ;2    3.0
  ;dtype: float64
  ```"
  [df-or-srs value & [attrs]]
  (u/kw-call df-or-srs "fillna" value attrs))

(defn not-na?
  "Returns a series or a data-frame of bools where false indicates a missing
  value

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Examples**

  ```
  (not-na? (series [1 nil 2]))
  ;0     True
  ;1    False
  ;2     True
  ;dtype: bool
  ```"
  [df-or-srs]
  (py/call-attr df-or-srs "notna"))

(defn all?
  "Return whether all elements are true. In Python only 0 and empty are
  considered false.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Attrs**

  - `:axis` -> {0 1 \"index\" \"columns\"}, default 0: 0 = \"index\" = row-wise,
  1 = \"columns\" = column-wise
  - `:bool-only` -> bool: include only bool columns. Doesn't work for series
  - `:level` -> int, str: indicate which level to count on for a multi index

  **Examples**

  ```
  (all? (series [1 nil 2]))
  ;true

  (all? (series [true false true]))
  ;false
  ```"
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "all" attrs))

(defn any?
  "Return whether any element is true. In Python only 0 and empty are
  considered false.

  **Arguments**

  - `df-or-srs` -> data-frame, series

  **Attrs**

  - `:axis` -> {0 1 \"index\" \"columns\"}, default 0: 0 = \"index\" = row-wise,
  1 = \"columns\" = column-wise
  - `:bool-only` -> bool: include only bool columns. Doesn't work for series
  - `:level` -> int, str: indicate which level to count on for a multi index

  **Examples**

  ```
  (any? (series [1 nil 2]))
  ;true

  (any? (series [true false true]))
  ;true
  ```"
  [df-or-srs & [attrs]]
  (u/simple-kw-call df-or-srs "any" attrs))

(defn select-rows
  "This is used for filtering by index. It can be either positional or label based
  and it can be a slice.
  
  **Arguments**:

  - `df-or-srs`: data-frame, series
  - `idx`: Iterable, series, slice, scalar: the needed indices
  - `how`: {:iloc :loc}, default :iloc: `:iloc` is purely positional,
  `:loc` is based on labels or booleans

  **Examples**:

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
  "Set the index of the given data-frame, this can be either a column,
  multiple columns or an Iterable.

  **Arguments**:

  - `df` -> data-frame
  - `cols` -> str, keyword, int, Iterable: can be the name of a column
  (string or value), a collection of column names or a collection of values

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

(defn factorize
  "TODO"
  [seq-or-srs & [attrs]]
  (u/kw-call u/pd "factorize" seq-or-srs attrs))
