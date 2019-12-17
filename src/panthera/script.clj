(ns panthera.script
  (:require
   [panthera.pandas.generics :as g]
   [libpython-clj.python :as py]))

(defrecord Data [data])

(defmethod print-method Data [v ^java.io.Writer w]
  (pr (vec (take 5 (:data v)))))

(defrecord DATASET [id cols data])

(defmethod print-method DATASET [v ^java.io.Writer w]
  (let [id (:id v)
        cols (:cols v)
        data (conj (vec (take 5 (:data v))) \u2026)]
    (clojure.pprint/pprint {:id id :cols cols :data data})))

(defmethod print-dup DATASET [v ^java.io.Writer w]
  (let [id   (:id v)
        cols (:cols v)
        data (conj (vec (take 5 (:data v))) "...")]
    (clojure.pprint/pprint {:id id :cols cols :data data})))

(defmulti to-clj
  (fn [obj] (identical? :series (py/python-type obj))))

(defmethod to-clj false
  [obj]
  (->DATASET
    (py/get-attr obj "index")
    (py/get-attr obj "columns")
    (lazy-seq (py/get-attr obj "values"))))

(defmethod to-clj true
  [obj]
  (->DATASET
    (py/get-attr obj "index")
    (or (py/get-attr obj "name") "unnamed")
    (lazy-seq (py/get-attr obj "values"))))

(defn -main [& args]
  (println (to-clj (g/series (vec (range 20))))))
