(ns panthera.numpy
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]))

(defonce numpy (py/import-module "numpy"))

(defonce sk (py/import-module "sklearn"))

(py/set-attr! sk "__SKLEARN_SETUP__" true)

(defn py-get-in
  [py-module v]
  (let [mods (drop-last v)]
    ((apply comp
            (reverse
             (map (fn [x] #(py/get-attr % x)) mods))) py-module)))

(defn doc
  [py-module ks])

(defn module
  [py-module]
  (fn [x]
      (fn
        ([]
         (if (seqable? x)
           (py/call-attr (py-get-in py-module x) (last x))
           (py/get-attr py-module x)))
        ([attrs]
         (if (seqable? x)
           (py/call-attr-kw (py-get-in py-module x)
                            (last x)
                            (vec (:args attrs))
                            (dissoc attrs :args))
           (py/call-attr-kw py-module x
                            (vec (:args attrs))
                            (dissoc attrs :args)))))))

(defn npy
  "Experiment:

  Usage -> (npy :shape {:args [[1 2 3]]}) => (3,)
  (npy [:linalg :norm] {:args [[[1 2 3] [4 5 6]]]}) => 9.539..."
  ([k] ((module numpy) k))
  ([k args] (((module numpy) k) args)))

(defn sklearn
  ([k] ((module sk) k))
  ([k args] (((module sk) k) args)))


(comment
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

  (def model (fit model (train-test split :x-train) (train-test split :y-train)))

  (defn predict
    [model x]
    (py/call-attr model "predict" x))

  (predict model (train-test split :x-test))

  (defn score
    [model x y]
    (py/call-attr model "score" x y))

  (score model (train-test split :x-test) (train-test split :y-test)))
