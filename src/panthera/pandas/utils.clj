(ns panthera.pandas.utils
  (:require
   [libpython-clj.python :as py]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [clojure.core.memoize :as m]))

(py/initialize!)

(defonce builtins (py/import-module "builtins"))
(defonce pd (py/import-module "pandas"))

(defn slice
  ([]
   (py/call-attr builtins "slice" nil))
  ([start]
   (py/call-attr builtins "slice" start))
  ([start stop]
   (py/call-attr builtins "slice" start stop))
  ([start stop incr]
   (py/call-attr builtins "slice" start stop incr)))

(defn pytype
  [obj]
  (py/python-type obj))

(def memo-key-converter
  (m/fifo csk/->snake_case_string {} :fifo/threshold 512))

(def memo-columns-converter
  (m/fifo csk/->kebab-case-keyword {} :fifo/threshold 512))

(defn keys->pyargs
  [m]
  (cske/transform-keys memo-key-converter m))

(defn ->clj
  [df-or-srs]
  (let [v  (py/get-attr df-or-srs "values")
        tp ({:data-frame "columns"
             :series     "name"} (pytype df-or-srs))
        ks (py/get-attr df-or-srs tp)]
    (if (= tp "columns")
      (map #(zipmap
             (map memo-columns-converter ks) %) v)
      (map #(hash-map (memo-columns-converter ks) % v)))))

(defn simple-kw-call
  [df kw & [attrs]]
  (py/call-attr-kw df kw []
                   (keys->pyargs attrs)))

(defn kw-call
  [df kw pos & [attrs]]
  (py/call-attr-kw df kw [pos]
                   (keys->pyargs attrs)))

(defn series?
  [obj]
  (identical? :series (pytype obj)))

(defn data-frame?
  [obj]
  (identical? :data-frame (pytype obj)))
