(ns panthera.pandas.reshape
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]))

(defn crosstab
  [df-or-srs & [attrs]]
  (u/kw-call u/pd "crosstab" df-or-srs attrs))

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
