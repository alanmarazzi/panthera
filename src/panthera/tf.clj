(ns panthera.tf
  (:require
   [panthera.pandas.utils :as u]
   [libpython-clj.python :as py]))

(defonce tensorflow (py/import-module "tensorflow"))

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

(defn cls
  [set-of-attrs py-module]
  (let [m (filter-vals set-of-attrs (py/att-type-map py-module))
        v (keys m)
        k (map u/memo-columns-converter v)
        f (fn [x]
            (fn
              ([]
               (py/call-attr py-module x))
              ([attrs]
               (py/call-attr-kw py-module x
                                (vec (:args attrs))
                                (dissoc attrs :args)))))
        o (map #(assoc {:type :type}
                       :doc (constantly
                             (py/get-attr
                              (py/get-attr py-module %)
                              "__doc__"))
                       :f (f %)) v)]
    (zipmap k o)))

(def consts (attrs #{:float} tensorflow))

(def functions (funcs #{:builtin-function-or-method
                         :function
                         :ufunc} tensorflow))

(def classes (cls #{:type
                    :generated-protocol-message-type} tensorflow))

(def np-merged
  (merge consts functions classes))

(defn np-caller
  [k]
  (let [c (k np-merged)]
    (case (:type c)
      :attr (:f c)
      :func (:f c)
      :type (:f c)
      :unsupported)))

(defn tf
  ([] (keys np-merged))
  ([k] ((np-caller k)))
  ([k args]
   (let [{:keys [doc]} args
         clean (dissoc args :doc)]
     (if doc
       (println ((:doc (k np-merged))))
       ((np-caller k) clean)))))

(def a (tf :constant {:args [(range 1 7)] :shape [2 3] :name :a}))
(def b (tf :constant {:args [(range 1 7)] :shape [3 2] :name :b}))
(def c (tf :matmul {:args [a b]}))

(def sess (tf :session {:config (tf :config-proto {:log_device_placement true})}))

(py/call-attr sess "run" c)
