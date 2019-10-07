(ns panthera.numpy
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]))

(defonce numpy (py/import-module "numpy"))

(defn py-get-in
  "A similar to `get-in` implementation for Python modules,
  classes and functions."
  [py-module v]
  (let [mods (drop-last v)]
    ((apply comp
            (reverse
             (map (fn [x] #(py/get-attr % x)) mods))) py-module)))

(defn doc
  "Use this to see modules and functions original docstrings.

  **Examples**

  ```
  (doc :power)

  (doc :linalg)

  (doc [:linalg :svd])
  ```"
  [ks]
  (if (seqable? ks)
    (println
     (py/get-attr
      (py/get-attr
       (py-get-in numpy ks)
       (last ks))
      "__doc__"))
    (println (py/get-attr (py/get-attr numpy ks) "__doc__"))))

(defn module
  [py-module]
  (fn [x]
      (fn
        ([]
         (if (seqable? x)
           (let [ks (map u/memo-key-converter x)]
             (py/get-attr (py-get-in py-module ks) (last ks)))
           (py/get-attr py-module (u/memo-key-converter x))))
        ([attrs]
         (if (seqable? x)
           (let [ks    (map u/memo-key-converter x)]
             (py/call-attr-kw (py-get-in py-module ks) (last ks)
                              (vec (:args attrs))
                              (u/keys->pyargs (dissoc attrs :args))))
           (py/call-attr-kw py-module (u/memo-key-converter x)
                            (vec (:args attrs))
                            (u/keys->pyargs (dissoc attrs :args))))))))

(defn npy
  "General method to access Numpy functions and attributes.

  By calling `(npy k)` you get either the value associated with that attribute
  (such as `(npy :nan)`) or the native Python function associated with that key.
  This is useful to pass functions around to other methods.

  By calling `(npy k {:args [my-args] :other-arg 2})` you're calling that method
  with the given arguments. `:args` is a conveniency argument to pass positional
  arguments to functions in the same order as you'd pass them to Numpy.
  This is because many Numpy functions have native C implementations that
  accept only positional arguments.

  For example `(npy :power {:args [[1 2] 2]})` will give back as a result
  `[1 4]` because we square (second element of `:args`) all the elements in the
  given `Iterable` (first element of `:args`)


  If you need to access a function in a submodule just pass a sequence of keys
  to `npy`, such as `(npy [:linalg :svd])`. The functioning of this is the same
  as above, but you'll be acting on the `:svd` function inside the `:linalg`
  submodule."
  ([k] (((module numpy) k)))
  ([k attrs] (((module numpy) k) attrs)))


(comment
  "An example on how to wrap another Python library, in this case scikit-learn"
  
  ; sklearn architecture is very convoluted, modules aren't loaded by default
  ; but only by explicit import. So we import everything as below
  (py/run-simple-string "from sklearn import *")
  (defonce sk (py/import-module "sklearn"))  

  (defn sklearn
    ([k] ((module sk) k))
    ([k args] (((module sk) k) args)))

  (def pokemon (pt/read-csv "resources/pokemon.csv"))

  (def split (sklearn [:model_selection :train_test_split]
                      {:args [(pt/subset-cols pokemon
                                              "HP" "Attack"
                                              "Defense" "Sp. Atk"
                                              "Sp. Def" "Speed")
                              (pt/subset-cols pokemon "Legendary")]
                       :test_size 0.3}))

  (defn train-test
    [split k]
    ((k {:x-train first
         :x-test  second
         :y-train #(% 2)
         :y-test  last}) split))

  (def logistic (sklearn [:linear_model :LogisticRegression]
                         {:n_jobs -1 :solver "lbfgs"}))

  (defn fit
    [model x y]
    (py/call-attr model "fit" x y))

  (def model (fit logistic (train-test split :x-train)
                  (train-test split :y-train)))

  (defn predict
    [model x]
    (py/call-attr model "predict" x))

  (predict model (train-test split :x-test))

  (defn score
    [model x y]
    (py/call-attr model "score" x y))

  (score model (train-test split :x-test) (train-test split :y-test)))
