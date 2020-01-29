(ns panthera.pandas.reshape
  (:refer-clojure
   :exclude [drop])
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]
   [panthera.pandas.generics :as g]))

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

(defn aggregate
  "Aggregate data using one or more functions over a given axis.

  This is very similar to `reduce`, but works on `data-frames` as well.

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`
  - `how` -> keyword, str, function, Iterable: how to aggregate data. This accepts
  either panthera functions strings/keywords, a list of the previous and/or user
  defined functions. Check examples for more info.

  **Attrs**

  - `:axis` -> {0 `:index` 1 `:columns`}, default 0: 0 = apply function along
  cols; 1 = apply function along rows
  - `fn-args` -> if the provided collapsing function needs arguments, just list
  them freely (see examples)

  **Examples**

  ```
  (def a (data-frame
         [[1, 2, 3]
          [4, 5, 6]
          [7, 8, 9]
          [##NaN, ##NaN, ##NaN]]
         {:columns [:A :B :C]}))

  (aggregate (series [1 2 3]) :sum)
  ;; 6

  (aggregate a [:sum :min])
  ;;         A     B     C
  ;; sum  12.0  15.0  18.0
  ;; min   1.0   2.0   3.0

  ; if `how` needs arguments, you can pass them as `attrs`
  (aggregate (series [1 2 3]) :cov {:other (series [4 5 6])})
  ;; 1.0

  (aggregate (series [1 2 3]) inc)
  ;; 0    2
  ;; 1    3
  ;; 2    4
  ;; dtype: int64
  ```
  "
  [df-or-srs how & [{:keys [axis fn-args] :as attrs}]]
  (u/kw-call df-or-srs "agg" how attrs))

(defn remap
  "Remap values in a series.

  This is the same as using `map` on a sequence while using a map as the mapped
  function: `(map {:a 1 :b 2} [:a :b]) => (1 2)`

  **Arguments**

  - `srs` -> `series`
  - `mappings` -> map, function: the mapping correspondence
  - `na-action` -> {`nil` `:ignore`}, default `nil`: `:ignore` doesn't pass missing
  values to the `mappings`

  **Examples**

  ```
  (remap (series [:a :b :c]) {:a 1 :b 2 :c 3})
  ;; 0    1
  ;; 1    2
  ;; 2    3
  ;; dtype: int64

  (remap (series [:a :b ##NaN]) #(str \"This is \" %))
  ;; 0      This is a
  ;; 1      This is b
  ;; 2    This is NaN
  ;; dtype: object

  (remap (series [:a :b ##NaN]) #(str \"This is \" %) :ignore)
  ;; 0      This is a
  ;; 1      This is b
  ;; 2            NaN
  ;; dtype: object
  ```
  "
  [srs mappings & [na-action]]
  (py/call-attr srs "map" mappings (or na-action nil)))

(defn groupby
  "Group `data-frame` or `series` by a given variable.

  Note that `groupby` does nothing by itself, this must be followed by another
  operation like aggregation.

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`
  - `by` -> str, keyword, Iterable, map, function: it can be a column, a list of
  columns, a function used to group the index, a collection of values to use as
  grouping variable

  **Attrs**

  - `:axis` -> {0 `:index` 1 `:columns`}: split along columns or rows
  - `:level` -> int, str, keyword, Iterable: if multiple index, group by this
  or these
  - `:as-index` -> bool, default `true`: when `false` this becomes basically
  as the SQL group by output
  - `:sort` -> bool, default `true`: if `false` you get a performance improvement
  - `:group-keys` -> bool, default `true`: add group keys to index when afterwards
  you call `apply`
  - `:squeeze` -> bool, default `false`: reduce dimensionality of the output if possible
  - `:observed` -> bool, default `false`: this only applies to Categoricals:
  if `true`, only show observed values for categorical groupers,
  if `false`, show all values for categorical groupers

  **Examples**

  ```
  (def a (data-frame {:animal [:falcon :falcon :parrot :parrot]
                        :max-speed [380 370 24 26]}))

  (-> a (r/groupby :animal) m/mean)
        max-speed
  ;; animal           
  ;; falcon        375
  ;; parrot         25

  (-> a (r/groupby :animal {:as-index false}) m/mean)
  ;;    animal  max-speed
  ;; 0  falcon        375
  ;; 1  parrot         25
  ```
  "
  [df-or-srs by & [{:keys [axis level as-index sort group-keys
                           squeeze observed] :as attrs}]]
  (u/kw-call df-or-srs "groupby" by attrs))

(defn rolling
  "Rolling window calculations

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`
  - `window` -> int, str. keyword: the size of the window. If str or keyword then
  this is considered as a time offset (e.g. :2s = 2 seconds, :30D = 30 days;
  check this for more options https://pandas.pydata.org/pandas-docs/stable/user_guide/timeseries.html#offset-aliases)

  **Attrs**

  - `:min-periods` -> int: minimum number of observations to have a value. For
  times the default is 1, otherwise the default is `window`
  - `:center` -> bool, default `false`: if `false` the result is set at the right
  edge of the window, otherwise it gets centered
  - `:win-type` -> str, keyword: refer to https://docs.scipy.org/doc/scipy/reference/signal.windows.html#module-scipy.signal.windows
  - `:on`-> str, keyword: column to use for the rolling window, only in case this
  is not the index
  - `:axis` -> {0 `:index` 1 `:columns`}: split along columns or rows
  - `:closed` -> {`:right` `:left` `:both` `:neither`}: where to make the interval
  close

  **Examples**
  ```
  (def a (data-frame {:b [0 1 2 3 4]}
           {:index
            (panthera.pandas.conversion/->datetime
              (series
                 [\"20130101 09:00:00\"
                  \"20130101 09:00:02\"
                  \"20130101 09:00:03\"
                  \"20130101 09:00:05\"
                  \"20130101 09:00:06\"]))}))

  (sum (rolling a 2))
  ;;                      b
  ;; 2013-01-01 09:00:00  NaN
  ;; 2013-01-01 09:00:02  1.0
  ;; 2013-01-01 09:00:03  3.0
  ;; 2013-01-01 09:00:05  5.0
  ;; 2013-01-01 09:00:06  7.0

  (sum (rolling a :2s))
  ;;                      b
  ;; 2013-01-01 09:00:00  0.0
  ;; 2013-01-01 09:00:02  1.0
  ;; 2013-01-01 09:00:03  3.0
  ;; 2013-01-01 09:00:05  3.0
  ;; 2013-01-01 09:00:06  7.0

  (sum (rolling a 2 {:win-type :triang}))
  ;;                      b
  ;; 2013-01-01 09:00:00  NaN
  ;; 2013-01-01 09:00:02  0.5
  ;; 2013-01-01 09:00:03  1.5
  ;; 2013-01-01 09:00:05  2.5
  ;; 2013-01-01 09:00:06  3.5

  (sum (rolling a 2 {:min-periods 1}))
  ;;                      b
  ;; 2013-01-01 09:00:00  0.0
  ;; 2013-01-01 09:00:02  1.0
  ;; 2013-01-01 09:00:03  3.0
  ;; 2013-01-01 09:00:05  5.0
  ;; 2013-01-01 09:00:06  7.0
  ```
  "
  [df-or-srs window & [{:keys [min-periods center win-type on axis closed]
                        :as attrs}]]
  (u/kw-call df-or-srs "rolling" window attrs))

(defn ewm
  "Exponentially weighted functions.

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`

  **Attrs**

  - `:com` -> numeric: decay in terms of center of mass
  - `:span` -> numeric: decay in terms of span
  - `:halflife` -> numeric: decay in terms of half-life
  - `:alpha` -> numeric: smoothing factor
  - `:min-periods` -> int, default 0: minimum number of observations
  - `:adjust` -> bool, default `true`: divide by decaying adjustment factor
  in beginning periods to account for imbalance in relative weightings
  - `:ignore-na` -> bool, default `false`: ignore missing values
  - `:axis` -> {0 `:index` 1 `:columns`}: use columns or rows

  **Examples**

  ```
  (def a (g/data-frame {:b [0 1 2 ##NaN 4]}))

  (-> a (ewm {:com 0.5}) mean)
  ;;           b
  ;; 0  0.000000
  ;; 1  0.750000
  ;; 2  1.615385
  ;; 3  1.615385
  ;; 4  3.670213

  (-> a (ewm {:span 3}) mean)
  ;;           b
  ;; 0  0.000000
  ;; 1  0.666667
  ;; 2  1.428571
  ;; 3  1.428571
  ;; 4  3.217391

  (-> a (ewm {:com 0.5 :ignore-na true}) mean)
  ;;           b
  ;; 0  0.000000
  ;; 1  0.750000
  ;; 2  1.615385
  ;; 3  1.615385
  ;; 4  3.225000
  ```
  "
  [df-or-srs & [{:keys [com span halflife min-periods adjust ignore-na axis]
                 :as   attrs}]]
  (u/simple-kw-call df-or-srs "ewm" attrs))

(defn drop
  "Drop requested rows or columns.

  Remove rows or columns by specifying label names and corresponding axis,
  or by specifying directly index or column names. When using a multi-index,
  labels on different levels can be removed by specifying the level.

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`
  - `labels` -> keyword, str, numeric, Iterable: index or labels to drop

  **Attrs**

  - `:axis` -> int, default 0: 0 = rows, 1 = columns
  - `:level` -> numeric, keyword, str: level to drop from multi index
  - `:errors` -> {`:ignore` `:raise`}, default `:raise`: ignore or raise errors

  **Examples**

  ```
  (require-python '[numpy :as np])
  (def df
    (data-frame
      (np/reshape (np/arange 12) [3 4])
      {:columns [:A :B :C :D]}))

  (drop df [:B :C] {:axis 1})
  ;;    A   D
  ;; 0  0   3
  ;; 1  4   7
  ;; 2  8  11

  (drop df [0 1])
  ;;    A  B   C   D
  ;; 2  8  9  10  11
  ```
  "
  [df-or-srs labels & [{:keys [axis level errors] :as attrs}]]
  (u/kw-call df-or-srs "drop" labels attrs))

(defn drop-rows
  "A shorthand for `(drop df [0 2] {:axis 0})`

  See [[drop]] docs for more info"
  [df rows & [{:keys [level errors] :as attrs}]]
  (drop df rows (merge attrs {:axis 0})))

(defn drop-cols
  "A shorthand for `(drop df [:A :C] {:axis 1})`

  See [[drop]] docs for more info"
  [df cols & [{:keys [level errors] :as attrs}]]
  (drop df cols (merge attrs {:axis 1})))

(defn dropna
  "Drop missing values.

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`

  **Attrs**

  - `:axis` -> int, default 0: 0 = rows, 1 = columns
  - `:how` -> {`:any` `:all`}, default `:any`: drop when there are `:any` missing
  values, or `:all` missing values
  - `:thresh` -> numeric: require `:thresh` missing values to drop
  - `:subset` -> Iterable: the subset to consider on opposite axis; e.g. if
  you drop rows `:subset` are the columns to consider for dropping

  **Examples**

  ```
  (def df
    (data-frame {:name [:Alfred :Batman :Robin]
                        :toy  [nil :Batmobile :Whip]
                        :born [nil :1940-04-25 nil]})

  (dropna df)
  ;;      name        toy        born
  ;; 1  Batman  Batmobile  1940-04-25
  ```
  "
  [df-or-srs & [{:keys [axis how thresh subset]
                 :as   attrs}]]
  (u/simple-kw-call df-or-srs "dropna" attrs))

(defn melt
  "Unpivot a `data-frame` from wide format to long format.

  Basically reshape the `data-frame` to have one row per observation and one
  column per variable

  **Arguments**

  - `df` -> `data-frame`

  **Attrs**

  - `:id-vars` -> Iterable: columns to use as identifiers
  - `:value-vars` -> Iterable: columns to melt (unpivot), if not specified uses
  all the columns not in `:id-vars`
  - `:var-name` -> keyword, str, default `:variable`: name for the variable column
  - `:value-name` -> keyword, str, default `:value`: name for the value column
  - `:col-level` -> numeric, str: the level to use for melting

  **Examples**

  ```
  (def df
    (transpose
      (data-frame [[:a :b :c] [1 3 5] [2 4 6]]
                  {:columns [0 1 2]
                   :index [:A :B :C]})))

  (melt df)
  ;;   variable value
  ;; 0        A     a
  ;; 1        A     b
  ;; 2        A     c
  ;; 3        B     1
  ;; 4        B     3
  ;; 5        B     5
  ;; 6        C     2
  ;; 7        C     4
  ;; 8        C     6

  (melt df {:id-vars [:A] :value-vars [:B]})
  ;;    A variable value
  ;; 0  a        B     1
  ;; 1  b        B     3
  ;; 2  c        B     5

  (melt df {:id-vars [:A] :value-vars [:B :C]})
  ;;    A variable value
  ;; 0  a        B     1
  ;; 1  b        B     3
  ;; 2  c        B     5
  ;; 3  a        C     2
  ;; 4  b        C     4
  ;; 5  c        C     6
  ```
  "
  [df & [{:keys [id-vars value-vars var-name
                 value-name col-level] :as attrs}]]
  (u/simple-kw-call df "melt" attrs))

(defn assign
  "Assign new columns to `df-or-srs`

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`
  - `cols` -> map: either a map `{:col-name value}`, or a map `{:col-name fn}`

  **Examples**

  ```
  (def df
    (transpose
      (data-frame [[:a :b :c] [1 3 5] [2 4 6]]
                  {:columns [0 1 2]
                   :index [:A :B :C]})))

  (assign df {:D 3})
  ;;    A  B  C  D
  ;; 0  a  1  2  3
  ;; 1  b  3  4  3
  ;; 2  c  5  6  3

  (assign df {:D [1 2 3]})
  ;;    A  B  C  D
  ;; 0  a  1  2  1
  ;; 1  b  3  4  2
  ;; 2  c  5  6  3

  (assign df {:D #(-> (subset-cols % :C) (mul 2))})
  ;;    A  B  C   D
  ;; 0  a  1  2   4
  ;; 1  b  3  4   8
  ;; 2  c  5  6  12
  ```
  "
  [df-or-srs cols]
  (py/call-attr-kw df-or-srs "assign" [] cols))

(defn stack
  "Stack the prescribed level(s) from columns to index.

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`

  **Attrs**

  - `:level` -> numeric, keyword, str, default -1: level to stack
  - `:dropna` -> bool, default true: drop rows with missing values if generated

  **Examples**

  ```
  (def df 
    (data-frame [[0 1] [2 3]]
                {:index [:cat :dog]
                 :columns [:weight :height]}))

  (stack df)
  ;; cat  weight    0
  ;;      height    1
  ;; dog  weight    2
  ;;      height    3
  ;; dtype: int64
  ```
  "
  [df-or-srs & [{:keys [level dropna] :as attrs}]]
  (u/simple-kw-call df-or-srs "stack" attrs))

(defn unstack
  "Pivot a level of the (necessarily hierarchical) index labels,
  returning a DataFrame having a new level of column labels whose inner-most
  level consists of the pivoted index labels.

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`

  **Attrs**

  - `:level` -> numeric, keyword, str, default -1: level to unstack
  - `:fill-value` -> any: replace missing values produced by `unstack` with this

  **Examples**

  ```
  (def s
    (stack
      (data-frame [[1 2] [3 4]]
                  {:index [:one :two]
                   :columns [:a :b]})))

  (unstack s)
  ;;      a  b
  ;; one  1  2
  ;; two  3  4

  (unstack s {:level 0})
  ;;    one  two
  ;; a    1    3
  ;; b    2    4

  (unstack (unstack s {:level 0}))
  ;; one  a    1
  ;;      b    2
  ;; two  a    3
  ;;      b    4
  ;; dtype: int64
  ```
  "
  [df-or-srs & [{:keys [level fill_value] :as attrs}]]
  (u/simple-kw-call df-or-srs "unstack" attrs))

(defn transpose
  "Transpose the given panthera object

  **Arguments**

  - `df-or-srs` -> `data-frame`, `series`

  **Examples**

  ```
  (def df (data-frame [[1 2 3] [4 5 6] [7 8 9]]))

  (transpose df)
  ;;    0  1  2
  ;; 0  1  4  7
  ;; 1  2  5  8
  ;; 2  3  6  9
  ```
  "
  [df-or-srs]
  (py/get-attr df-or-srs "T"))
