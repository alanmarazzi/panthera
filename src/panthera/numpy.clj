(ns panthera.numpy
  (:require
    [panthera.pandas.utils :as u]
    [libpython-clj.python :as py]))

(defonce numpy (py/import-module "numpy"))

(defn filter-vals
  [pred m]
  (into {} (filter (fn [[k v]] (pred v))
                   m)))

(defn attrs
  [set-of-attrs py-module]
  (let [m (filter-vals set-of-attrs (py/att-type-map py-module))
        v (keys m)
        k (map u/memo-columns-converter v)
        f (fn [x] (fn [] (py/get-attr py-module x)))
        o (map #(assoc {:doc  (constantly "Constant")
                        :type :attr}
                  :f (f %)) v)]
    (zipmap k
            o)))

(defn funcs
  [set-of-attrs py-module]
  (let [m (filter-vals set-of-attrs (py/att-type-map py-module))
        v (keys m)
        k (map u/memo-columns-converter v)
        f (fn [x]
            (fn
              ([]
               (py/get-attr py-module x))
              ([attrs]
               (py/call-attr-kw py-module x
                                (vec (:args attrs))
                                (dissoc attrs :args)))))
        o (map #(assoc {:type :func}
                  :doc (constantly
                         (py/get-attr
                           (py/get-attr py-module %)
                           "__doc__"))
                  :f (f %)) v)]
    (zipmap k o)))

(def consts (attrs #{:float} numpy))

(def functions (funcs #{:builtin-function-or-method
                        :function
                        :ufunc} numpy))

(def np-merged
  (merge consts functions))

(defn np-caller
  [k]
  (let [c (k np-merged)]
    (case (:type c)
      :attr (:f c)
      :func (:f c)
      :unsupported)))

(defn npy
  "General method to access Numpy functions and
  attributes.

  By calling `(npy)` you get a list of available
  keys that correspond to homonymous Numpy
  attributes and functions that are available.

  By calling `(npy k)` you get either the value
  associated with that attribute (such as
  `(npy :nan)`) or the native Python function
  associated with that key. This is useful to
  pass functions around to other methods.

  By calling `(npy k {:doc true})` you get the
  original docstring of that function, while
  for attributes you'll get 'Constant'.

  By calling `(npy k {:args [my-args] :other-arg 2})`
  you're calling that method with the given arguments.
  `:args` is a conveniency argument to pass positional
  arguments to functions in the same order as you pass
  them. This is because many Numpy functions have native
  C implementations that accept only positional arguments.

  For example `(npy :power {:args [[1 2] 2]})` will
  give back as a result `[1 4]` because we square
  (second element of `:args`) all the elements in the
  given `Iterable` (first element of `:args`)"
  ([] (keys np-merged))
  ([k] ((np-caller k)))
  ([k args]
   (let [{:keys [doc]} args
         clean (dissoc args :doc)]
     (if doc
       (println ((:doc (k np-merged))))
       ((np-caller k) clean)))))