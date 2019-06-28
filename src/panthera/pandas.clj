(ns panthera.pandas
  (:require
   [libpython-clj.python :as py]
   [clojure.pprint :as pp]
   [tech.v2.datatype :as dtype]
   [tech.v2.datatype.functional :as dfn])
  (:import
   [java.io Writer]))

(py/initialize!)

(defonce pandas (py/import-module "pandas"))
(defonce np (py/import-module "numpy"))
(defonce builtins (py/import-module "builtins"))

(defn slice
  ([]
   (py/call-attr builtins "slice" nil))
  ([start]
   (py/call-attr builtins "slice" start))
  ([start stop]
   (py/call-attr builtins "slice" start stop))
  ([start stop incr]
   (py/call-attr builtins "slice" start stop incr)))

(defn shape
  [df-or-series]
  (py/get-attr df-or-series "shape"))

(defn n-rows
  [df-or-series]
  ((shape df-or-series) 0))

(defn n-cols
  [df]
  ((shape df) 1))

(defn subset-cols
  [df & colnames]
  (let [cols (if (= 1 (count colnames))
               (first colnames)
               (apply py/->py-list [colnames]))]
    (py/get-item df cols)))

(defn subset-rows
  [df & slicing]
  (py/get-item
   (py/get-attr df "iloc")
   (apply slice slicing)))

(defn read-excel
  [& attrs]
  (apply #(py/call-attr-kw pandas "read_excel" %1 %2) attrs))

(def prova (-> (read-excel ["benchmark.xlsx"] {"sheet_name" 1
                                               "na_values"  ["n.d." "n.s."]})
               (py/call-attr-kw "dropna" [] {"subset" ["Ragione sociale"]
                                             "axis"   0})))

(defn pandas->clj
  [df-or-series]
  (map #(into {} %) (vec (py/call-attr df-or-series "to_dict" "records"))))

(defn melt
  [df]
  (py/call-attr-kw pandas "melt" [df]
                   {}))

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

(defn index
  [df-or-series]
  (py/call-attr pandas "Series"
                (py/get-attr df-or-series "index")))

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
  (with-meta
    (let [by (flatten (vector by))]
      (py/call-attr-kw df "sort_values"
                       []
                       {"by"          (py/->py-list by)
                        "ascending"   ascending
                        "kind"        kind
                        "na_position" na-position
                        "axis"        axis}))
    {:type :pandas}))

(defn data-frame
  [data]
  (py/call-attr pandas "DataFrame" data))

(defn dropna
  [df-or-series & [attrs]]
  (py/call-attr-kw df-or-series "dropna" [] (keyword->pyarg attrs)))

(defn filter-pd
  [df-or-series colname pred]
  (as-> df-or-series ds
    (subset-cols ds colname)
    (dropna ds)
    (keep-indexed (fn [id it]
                    (when pred
                      id)) ds)
    (py/get-item
     (py/get-attr df-or-series "iloc")
     ds)))

(defn mean
  [df-or-series]
  (py/call-attr df-or-series "mean"))

(defn add-cols
  [df-or-series cols]
  (py/call-attr-kw df-or-series "assign" []
                   (keyword->pyarg cols)))

(defn tail
  [df-or-series & [n]]
  (py/call-attr df-or-series "tail" (or n 5)))

(defn value-counts
  [df & [attrs]]
  (py/call-attr-kw df "value_counts" [] (keyword->pyarg attrs)))

(defn fillna
  [df-or-series value & [attrs]]
  (py/call-attr-kw df-or-series "fillna" [value]
                   (keyword->pyarg attrs)))

(defn pdtype
  [df-or-series]
  (py/get-attr df-or-series "dtypes"))

(defn pd-concat
  [df1 df2 & [attrs]]
  (py/call-attr-kw pandas "concat" [[df1 df2]]
                   (keyword->pyarg attrs)))

(defn pivot
  [df & [attrs]]
  (py/call-attr-kw df "pivot" [] (keyword->pyarg attrs)))

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

(-> prova
    (py/call-attr-kw "dropna" [] {"subset" ["Ragione sociale"]
                                  "axis"   0})
    (py/call-attr-kw "sort_values" [] {"by"        "Ricavi"
                                       "ascending" false})
    (py/call-attr "reset_index")
    (py/call-attr-kw "drop" ["index"] {"axis" 1})
    (py/call-attr "head")
    (py/get-item "Ragione sociale"))

(as-> prova p
  (py/call-attr-kw p "dropna" [] {"subset" ["Ragione sociale"]
                                  "axis"   0})
  (py/call-attr-kw p "sort_values" [] {"by"        "Ricavi"
                                       "ascending" false})
  (py/call-attr p "reset_index")
  (py/call-attr-kw p "drop" ["index"] {"axis" 1})
  (py/call-attr-kw p "assign" []
                   {"Delta" (let [d (py/get-item p "Debiti")
                                  c (py/get-item p "Crediti")]
                              (py/call-attr d "sub" c))}))

(as-> prova p
    (subset-cols p "Debiti")
    (py/call-attr p "dropna")
    (map #(> 20 %) p))
