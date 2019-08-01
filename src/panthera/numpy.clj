(ns panthera.numpy
  (:require
    [libpython-clj.python :as py]))

(defonce numpy (py/import-module "numpy"))

(defn py-get-in
  [py-module v]
  (let [mods (drop-last v)]
    ((apply comp
            (reverse
             (map (fn [x] #(py/get-attr % x)) mods))) py-module)))

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
