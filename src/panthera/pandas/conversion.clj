(ns panthera.pandas.conversion
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]))

(defn ->numeric
  [seq-or-srs & [attrs]]
  (u/simple-kw-call u/pd "to_numeric" seq-or-srs attrs))

(defn ->datetime
  [seq-or-srs & [attrs]]
  (u/simple-kw-call u/pd "to_datetime" seq-or-srs attrs))

(defn ->timedelta
  [seq-or-srs & [attrs]]
  (u/simple-kw-call u/pd "to_timedelta" seq-or-srs attrs))

;; bdate_range and period_range are just date_range with freq defaults
(defn date-range
  [& [attrs]]
  (u/simple-kw-call u/pd "date_range" attrs))

(defn timedelta-range
  [& [attrs]]
  (u/simple-kw-call u/pd "timedelta_range" attrs))

(defn infer-time-freq
  [seq-or-srs & [warn]]
  (u/kw-call u/pd "infer_freq" seq-or-srs {:warn (or warn true)}))

(defn astype
  [df-or-srs dtype & [attrs]]
  (u/kw-call df-or-srs "astype" dtype attrs))
