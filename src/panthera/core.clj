(ns panthera.core
  (:require
   [t6.from-scala.core :refer [$ $$] :as $])
  (:import
   (org.saddle.vec VecAny)
   (scala Product Tuple2 Tuple3)
   (scala.collection.immutable List IndexedSeq ListMap)
   (scala.collection JavaConversions Map)
   (scala Option Array)))

(def iris (slurp "https://gist.githubusercontent.com/netj/8836201/raw/6f9306ad21398ea43cba4f7d537619d0e07d5ae3/iris.csv"))

($ Vec ($ Array [1 2 3]))
($ List & 1 2 3)
($ VecAny )
