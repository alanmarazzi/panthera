(ns panthera.pandas.reshape
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]
   [panthera.pandas.generics :as g]

   [libpython-clj.python.protocols :as p]))

(defn crosstab
  "Compute a cross tabulation of two (or more) factors. By default
  computes a frequency table of the factors unless an array of values and an
  aggregation function are passed.

  **Arguments**

  - `seq-or-srs` -> seqable, `series`

  **Attrs**

  - `:columns` -> Iterable, `series`, Iterable of Iter/srs: values to group by
  - `:values` -> Iterable, `series`, Iterable of Iter/srs: values to group
  according to factors, requires `:aggfunc`
  - `:rownames` -> Iterable, `series`: the names of `seq-or-srs`
  - `:colnames` -> Iterable, `series`: the names of `:columns`
  - `:aggfunc` -> function, keyword, str: the aggregation function, requires
  `:values`. It can be a panthera function (`sum`), a numpy function (`(npy :sum)`),
  the name of a numpy function (`:mean` or \"mean\") or a Clojure function. In the
  latter case be aware that you have to reduce over a map.
  - `:margins` -> bool, default `false`: add subtotals
  - `:margins-name`: str, default \"All\": name of the row/column holding totals
  when `:margins` true
  - `:dropna` -> bool, default `true`: exclude columns with all missing values
  - `:normalize` -> bool, {`:all` `:index` `columns`}, {0 1}, default `false`:
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
  [seq-or-srs & [{:keys [columns values rownames colnames aggfunc
                         margins margins-name dropna normalize]
                  :as attrs}]]
  (u/kw-call u/pd "crosstab" seq-or-srs attrs))

(defn pivot
  "Returns a stacked `data-frame`: basically changes it from long format to wide.

  **Arguments**

  - `df` -> `data-frame`

  **Attrs**

  - `:index` -> str, keyword, default `nil`: the column to use as the new index.
  When `nil` uses the current one
  - `:columns` -> str, keyword: columns to use for the new `data-frame`
  - `:values` -> str, keyword, Iterable, default `nil`: columns to use to populate
  values. If `nil` all remaining columns will be used

  **Examples**

  ```
  (def df (data-frame {:foo [:one :one :one :two :two :two]
                       :bar [:a :b :c :a :b :c]
                       :baz [1 2 3 4 5 6]
                       :zoo [:x :y :z :q :w :t]}))

  (pivot df {:columns :bar :index :foo})
  ;;     baz       zoo      
  ;; bar   a  b  c   a  b  c
  ;; foo                    
  ;; one   1  2  3   x  y  z
  ;; two   4  5  6   q  w  t
  
  (pivot df {:index :foo :columns :bar :values [:baz :zoo]})
  ;;     baz       zoo      
  ;; bar   a  b  c   a  b  c
  ;; foo                    
  ;; one   1  2  3   x  y  z
  ;; two   4  5  6   q  w  t
  ```
  "
  [df & [{:keys [index columns values]
          :as attrs}]]
  (u/simple-kw-call df "pivot" attrs))

(defn cut
  "Bin the given values into categories.

  Use this when you want to go from continuous values to ordered categories. For
  example, you could go from age to age ranges.

  N.B.: `cut` converts your values to a [`Categorical`](https://pandas.pydata.org/pandas-docs/stable/reference/api/pandas.Categorical.html#pandas.Categorical) type. This
  means that you can choose whether you want a label back or just the new value.

  **Arguments**

  - `seq-or-srs` -> seqable or `series`
  - `bins` -> int, Iterable, `series`: how to bin the data. If int defines the number
  of equal-width bins, otherwise values are treated as bins edges

  **Attrs**

  - `:right` -> bool, default `true`: include the rightmost edge?
  - `:labels` -> Iterable, bool: if Iterable, specifies the labels for the bins,
  if false it doesn't return the labels, only the values (**N.B.: the suggestion
  is to work with `{:labels false}` as much as possible, especially if you have to
  convert things to Clojure at some point**)
  - `:retbins` -> bool, default `false`: return bins?
  - `:precision` -> int, default 3: the precision of the bins labels
  - `:include-lowest` -> bool, default `false`: should the first interval be left-inclusive?
  - `:duplicates` -> {`:raise`, `:drop`, `nil`}: ff bin edges are not unique,
  raise error or drop non-uniques

  **Examples**

  ```
  (def s (series [1 7 5 4 6 3]))

  (cut s 3)
  ;; 0    (0.994, 3.0]
  ;; 1      (5.0, 7.0]
  ;; 2      (3.0, 5.0]
  ;; 3      (3.0, 5.0]
  ;; 4      (5.0, 7.0]
  ;; 5    (0.994, 3.0]
  ;; dtype: category
  ;; Categories (3, interval[float64]): [(0.994, 3.0] < (3.0, 5.0] < (5.0, 7.0]]

  (cut s [3 5 7])
  ;; 0           NaN
  ;; 1    (5.0, 7.0]
  ;; 2    (3.0, 5.0]
  ;; 3    (3.0, 5.0]
  ;; 4    (5.0, 7.0]
  ;; 5           NaN
  ;; dtype: category
  ;; Categories (2, interval[int64]): [(3, 5] < (5, 7]]

  (cut s 3 {:labels false})
  ;; 0    0
  ;; 1    2
  ;; 2    1
  ;; 3    1
  ;; 4    2
  ;; 5    0
  ;; dtype: int64
  ```
  "
  [seq-or-srs bins & [{:keys [right labels retbins precision
                              include-lowest duplicates]
                       :as attrs}]]
  (py/call-attr-kw u/pd "cut" [seq-or-srs bins]
                   (u/keys->pyargs attrs)))

(defn qcut
  "Bin values into quantiles.

  The same as `cut`, but categories are quantiles.

  **Arguments**

  - `seq-or-srs` -> seqable or `series`
  - `q` -> int, Iterable: either number of quantiles or Iterable of quantiles

  **Attrs**

  - `:labels` -> Iterable, bool: if Iterable, specifies the labels for the bins,
  if false it doesn't return the labels, only the values (**N.B.: the suggestion
  is to work with `{:labels false}` as much as possible, especially if you have to
  convert things to Clojure at some point**)
  - `:retbins` -> bool, default `false`: return bins?
  - `:precision` -> int, default 3: the precision of the bins labels
  - `:duplicates` -> {`:raise`, `:drop`, `nil`}: ff bin edges are not unique,
  raise error or drop non-uniques

  **Examples**

  ```
  (qcut (range 5) 4)
  ;; [(-0.001, 1.0], (-0.001, 1.0], (1.0, 2.0], (2.0, 3.0], (3.0, 4.0]]
  ;; Categories (4, interval[float64]): [(-0.001, 1.0] < (1.0, 2.0] < (2.0, 3.0] < (3.0, 4.0]]

  (qcut (range 5) 3 {:labels [:low :medium :high]})
  ;; [low, low, medium, high, high]
  ;; Categories (3, object): [low < medium < high]

  (qcut (range 5) 3 {:labels false})
  ;; [0 0 1 2 2]
  ```
  "
  [seq-or-srs q & [{:keys [labels retbins precision duplicates]
                    :as attrs}]]
  (py/call-attr-kw u/pd "qcut" [seq-or-srs q]
                   (u/keys->pyargs attrs)))

(defn merge-ordered
  "Merge two `data-frames` together, facilities to deal with ordered data.

  **Arguments**

  - `left` -> `data-frame`
  - `right` -> `data-frame`

  **Attrs**

  - `:on` -> str, keyword, Iterable: column names to be joined on. They must be the
  same in both `left` and `right`
  - `:left-on` -> str, keyword, Iterable, `series`: columns to join on the `left`,
  use this if you have different columns names
  - `:right-on` -> str, keyword, Iterable, `series`: columns to join on the `right`,
  use this if you have different columns names
  - `:left-by` -> str, keyword, Iterable, `series`: groupby `left` on the given
  columns and then join piece by piece
  - `:right-by` -> str, keyword, Iterable, `series`: groupby `right` on the given
  columns and then join piece by piece
  - `:fill-method` -> {`:ffill` `nil`}, default `nil`: forward fill missing data
  - `:suffixes` -> Iterable, default [`:_x` `:_y`]: the suffixes to add to overlapping
  column names
  - `:how` -> {`:left` `:right` `:outer` `:inner`}, default `:outer`: kind of join

  **Examples**
  ```
  (def A
    (data-frame
      {:key    [:a :c :e :a]
       :lvalue [1 2 3 1]
       :group  [:a :a :a :b]}))

  (def B
    (data-frame
      {:key [:b :c :d]
       :rvalue [1 2 3]}))

  (merge-ordered A B)
  ;;   key  lvalue group  rvalue
  ;; 0   a     1.0     a     NaN
  ;; 1   a     1.0     b     NaN
  ;; 2   b     NaN   NaN     1.0
  ;; 3   c     2.0     a     2.0
  ;; 4   d     NaN   NaN     3.0
  ;; 5   e     3.0     a     NaN

  (merge-ordered A B {:fill-method :ffill})
  ;;   key  lvalue group  rvalue
  ;; 0   a       1     a     NaN
  ;; 1   a       1     b     NaN
  ;; 2   b       1     b     1.0
  ;; 3   c       2     a     2.0
  ;; 4   d       2     a     3.0
  ;; 5   e       3     a     3.0

  (merge-ordered A B {:fill-method :ffill :left-by \"group\"})
  ;;   key  lvalue group  rvalue
  ;; 0   a       1     a     NaN
  ;; 1   b       1     a     1.0
  ;; 2   c       2     a     2.0
  ;; 3   d       2     a     3.0
  ;; 4   e       3     a     3.0
  ;; 5   a       1     b     NaN
  ;; 6   b       1     b     1.0
  ;; 7   c       1     b     2.0
  ;; 8   d       1     b     3.0

  (merge-ordered A B {:left-on :lvalue :right-on :rvalue})
  ;;   key_x  lvalue group key_y  rvalue
  ;; 0     a       1     a     b       1
  ;; 1     a       1     b     b       1
  ;; 2     c       2     a     c       2
  ;; 3     e       3     a     d       3
  ```
  "
  [left right & [{:keys [on left-on right-on left-by right-by
                         fill-method suffixes how]
                  :as attrs}]]
  (py/call-attr-kw u/pd "merge_ordered" [left right]
                   (u/keys->pyargs attrs)))

(defn merge-asof
  "Similar to a left join, but merges on nearest key rather than equal.

  **Arguments**

  - `left` -> `data-frame`: sorted by key
  - `right` -> `data-frame`: sorted by key

  **Attrs**

  - `:on` str, keyword -> column name to join on. Must be in both `data-frames` and
  it must be ordered and numeric (dates, int, etc)
  - `:left-on` -> str, keyword: column name to join in left `data-frame`. The
  requirements are the same as for `:on`
  - `:right-on` -> str, keyword: column name to join in right `data-frame`. The
  requirements are the same as for `:on`
  - `:left-index` -> bool: index of left `data-frame` is the join key?
  - `:right-index` -> bool: index of right `data-frame` is the join key?
  - `:by` -> str, keyword, Iterable, `series`: match these columns before merging
  - `:left-by` -> str, keyword, Iterable. `series`: as `:by` but only for left `data-frame`
  - `:right-by` -> str, keyword, Iterable. `series`: as `:by` but only for right `data-frame`
  - `:suffixes` -> Iterable: suffix to add to overlapping column names, must
  have length 2 and the first one is `left` and second one is `right`
  - `:tolerance` -> depends on key: the tolerance for merging
  - `:allow-exact-matches` -> bool, default `true`: allow matching with same `:on` value?
  - `:direction` -> {`:backward` `:forward` `:nearest`}, default `:backward`: search for
  prior, subsequent or closest matches

  **Examples**

  ```
  (def trades
    (data-frame
      {:time (->datetime [\"2016-05-25 13:30:00.023\"
                          \"2016-05-25 13:30:00.038\"
                          \"2016-05-25 13:30:00.048\"
                          \"2016-05-25 13:30:00.048\"])
       :ticker [:MSFT :MSFT :GOOG :AAPL]
       :price [51.95 51.95 720.77 98.00]
       :quantity [75 155 100 100]}))

  (def quotes
    (data-frame
      {:time (->datetime [\"2016-05-25 13:30:00.023\"
                          \"2016-05-25 13:30:00.023\"
                          \"2016-05-25 13:30:00.030\"
                          \"2016-05-25 13:30:00.048\"
                          \"2016-05-25 13:30:00.049\"])
       :ticker [:GOOG :MSFT :MSFT :GOOG :AAPL]
       :bid [720.5 51.95 51.97 720.5 97.99]
       :ask [720.93 51.96 51.98 720.93 98.01]}))

  (merge-asof trades quotes {:on :time})
  ;;                      time ticker_x   price  quantity ticker_y     bid     ask
  ;; 0 2016-05-25 13:30:00.023     MSFT   51.95        75     MSFT   51.95   51.96
  ;; 1 2016-05-25 13:30:00.038     MSFT   51.95       155     MSFT   51.97   51.98
  ;; 2 2016-05-25 13:30:00.048     GOOG  720.77       100     GOOG  720.50  720.93
  ;; 3 2016-05-25 13:30:00.048     AAPL   98.00       100     GOOG  720.50  720.93

  (merge-asof trades quotes {:on :time :allow-exact-matches false})
  ;;                      time ticker_x   price  quantity ticker_y    bid    ask
  ;; 0 2016-05-25 13:30:00.023     MSFT   51.95        75      NaN    NaN    NaN
  ;; 1 2016-05-25 13:30:00.038     MSFT   51.95       155     MSFT  51.97  51.98
  ;; 2 2016-05-25 13:30:00.048     GOOG  720.77       100     MSFT  51.97  51.98
  ;; 3 2016-05-25 13:30:00.048     AAPL   98.00       100     MSFT  51.97  51.98

  (merge-asof trades quotes {:on :time :direction :forward})
  ;;                      time ticker_x   price  quantity ticker_y    bid     ask
  ;; 0 2016-05-25 13:30:00.023     MSFT   51.95        75     GOOG  720.5  720.93
  ;; 1 2016-05-25 13:30:00.038     MSFT   51.95       155     GOOG  720.5  720.93
  ;; 2 2016-05-25 13:30:00.048     GOOG  720.77       100     GOOG  720.5  720.93
  ;; 3 2016-05-25 13:30:00.048     AAPL   98.00       100     GOOG  720.5  720.93

  (merge-asof trades quotes {:on :time :by :ticker})
  ;;                      time ticker   price  quantity     bid     ask
  ;; 0 2016-05-25 13:30:00.023   MSFT   51.95        75   51.95   51.96
  ;; 1 2016-05-25 13:30:00.038   MSFT   51.95       155   51.97   51.98
  ;; 2 2016-05-25 13:30:00.048   GOOG  720.77       100  720.50  720.93
  ;; 3 2016-05-25 13:30:00.048   AAPL   98.00       100     NaN     NaN
  ```
  "
  [left right & [{:keys [on left-on right-on left-index right-index by
                         left-by right-by suffixes tolerance
                         allow-exact-matches direction]
                  :as   attrs}]]
  (py/call-attr-kw u/pd "merge_asof" [left right]
                   (u/keys->pyargs attrs)))

(defn concatenate
  "Append `series`es and/or `data-frame`s along a wanted axis.

  **Arguments**

  - `dfs-or-srss` -> Iterable: a collection of multiple `series`/`data-frame`

  **Attrs**

  - `:axis` -> int, default 0: 0 = rows, 1 = columns
  - `:join` -> {`:inner` `:outer`}, default `:outer`: the kind of join on other `:axis`
  - `:ignore-index` -> bool, default `false`: whether to consider the index along
  the wanted `:axis`
  - `:keys` -> Iterable, default `nil`: this lets you build a hierarchical index
  using the passed `:keys` as the outermost levels
  - `:levels` -> Iterable, default `nil`: unique values for building a multi index
  - `:names` -> Iterable, default `nil`: names of the levels in the hierarchical index
  - `:verify-integrity` -> bool, default `false`: does the new `:axis`
  contain duplicates? (P.S.: expensive operation)
  - `:sort` -> bool, default `true`: sort the other `:axis` when `:join` is `:outer`
  - `:copy` -> bool, default `true`: if `false` avoid copying when unnecessary

  **Examples**

  ```
  (concatenate [(series (range 3)) (series (range 3))])
  ;; 0    0
  ;; 1    1
  ;; 2    2
  ;; 0    0
  ;; 1    1
  ;; 2    2
  ;; dtype: int64

  (concatenate [(series (range 3)) (series (range 3))] {:axis 1})
  ;;    0  1
  ;; 0  0  0
  ;; 1  1  1
  ;; 2  2  2

  (concatenate [(data-frame {:a [1 2 3] :b [4 5 6]})
                (data-frame {:a [2 2 2] :b [3 3 3]})])
  ;;    a  b
  ;; 0  1  4
  ;; 1  2  5
  ;; 2  3  6
  ;; 0  2  3
  ;; 1  2  3
  ;; 2  2  3

  (concatenate [(data-frame {:a [1 2 3] :b [4 5 6]})
                (data-frame {:a [2 2 2] :b [3 3 3]})]
               {:ignore-index true})
  ;;    a  b
  ;; 0  1  4
  ;; 1  2  5
  ;; 2  3  6
  ;; 3  2  3
  ;; 4  2  3
  ;; 5  2  3
  ```
  "
  [dfs-or-srss & [{:keys [axis join ignore-index keys levels
                          names verify-integrity sort copy]
                   :as attrs}]]
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
