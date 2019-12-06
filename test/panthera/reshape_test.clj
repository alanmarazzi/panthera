(ns panthera.reshape-test
  (:require
   [clojure.test :refer :all]
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u :reload true]
   [panthera.pandas.generics :as g]
   [panthera.pandas.reshape :as r :reload true]
   [panthera.pandas.math :as m :reload true]))

(defn filter-nan
  [d]
  (into [] (comp (mapcat vals) (filter (complement #(.isNaN %)))) d))

(deftest crosstab
  (are [r c o]
       (= (u/->clj (r/crosstab r {:columns c})) o)
    [[]] [[]] []
    [[1 2 2]] [[:a :b :a]] [{:a 1 :b 0} {:a 1 :b 1}]
    (g/series [1 2 3]) [[:a :b :a]] [{:a 1 :b 0} {:a 0 :b 1} {:a 1 :b 0}])
  (are [r d o]
      (= (filter-nan (u/->clj (r/crosstab r d))) o)
    [[1 2 2]] {:columns [[:a :b :b]]
               :values  [10 20 30]
               :aggfunc :mean} [10.0 25.0])
  (is (= (u/->clj
           (r/crosstab [[1 2 2]] {:columns [[:a :b :a]] :margins true}))
        [{:a 1 :b 0 :all 1}
         {:a 1 :b 1 :all 2}
         {:a 2 :b 1 :all 3}])))

(deftest pivot
  (are [d o]
      (= (u/->clj (r/pivot (g/data-frame {:foo [:one :one :one :two :two :two]
                                          :bar [:a :b :c :a :b :c]
                                          :baz [1 2 3 4 5 6]
                                          :zoo [:x :y :z :q :w :t]})
                    d)) o)

    {:columns :bar :index :foo} [{[:baz :a] 1,
                                  [:baz :b] 2,
                                  [:baz :c] 3,
                                  [:zoo :a] "x",
                                  [:zoo :b] "y",
                                  [:zoo :c] "z"}
                                 {[:baz :a] 4,
                                  [:baz :b] 5,
                                  [:baz :c] 6,
                                  [:zoo :a] "q",
                                  [:zoo :b] "w",
                                  [:zoo :c] "t"}]

    {:index :foo :columns :bar :values [:baz :zoo]} [{[:baz :a] 1,
                                                      [:baz :b] 2,
                                                      [:baz :c] 3,
                                                      [:zoo :a] "x",
                                                      [:zoo :b] "y",
                                                      [:zoo :c] "z"}
                                                     {[:baz :a] 4,
                                                      [:baz :b] 5,
                                                      [:baz :c] 6,
                                                      [:zoo :a] "q",
                                                      [:zoo :b] "w",
                                                      [:zoo :c] "t"}]))

(deftest cut
  (is
    (->> (u/->clj (r/cut (g/series [1 7 5 4 6 3]) 3))
      first
      vals
      first
      (m/eq (u/simple-kw-call u/pd "Interval" {:left 0.994 :right 3.0}))))
  (are [b d o]
      (= (u/->clj (r/cut (g/series [1 7 5 4 6 3]) b d)) o)
    3 {:labels false} [{:unnamed 0} {:unnamed 2} {:unnamed 1}
                       {:unnamed 1} {:unnamed 2} {:unnamed 0}]

    3 {:labels [:a :b :c]} [{:unnamed "a"} {:unnamed "c"} {:unnamed "b"}
                            {:unnamed "b"} {:unnamed "c"} {:unnamed "a"}]

    [0 3 5 7] {:labels false} [{:unnamed 0} {:unnamed 2} {:unnamed 1}
                               {:unnamed 1} {:unnamed 2} {:unnamed 0}]))

(deftest qcut
  (is
    (->> (u/->clj (r/cut (g/series (range 5)) 4))
      first
      vals
      first
      (m/eq (u/simple-kw-call u/pd "Interval" {:left -0.004 :right 1.0}))))
  (are [b d o]
      (= (u/->clj (r/cut (g/series (range 5)) b d)) o)
    3 {:labels false} [{:unnamed 0} {:unnamed 0}
                       {:unnamed 1} {:unnamed 2}
                       {:unnamed 2}]

    3 {:labels [:low :medium :high]} [{:unnamed "low"}
                                      {:unnamed "low"}
                                      {:unnamed "medium"}
                                      {:unnamed "high"}
                                      {:unnamed "high"}]))

(deftest merge-ordered
  (let [a (g/data-frame
            {:key    [:a :c :e :a]
             :lvalue [1 2 3 1]
             :group  [:a :a :a :b]})
        b (g/data-frame
            {:key    [:b :c :d]
             :rvalue [1 2 3]})]
    (are [d o]
        (m/same? (r/merge-ordered a b d) (g/data-frame o))
      {} [{:key "a", :lvalue 1.0, :group "a", :rvalue ##NaN}
          {:key "a", :lvalue 1.0, :group "b", :rvalue ##NaN}
          {:key "b", :lvalue ##NaN, :group ##NaN, :rvalue 1.0}
          {:key "c", :lvalue 2.0, :group "a", :rvalue 2.0}
          {:key "d", :lvalue ##NaN, :group ##NaN, :rvalue 3.0}
          {:key "e", :lvalue 3.0, :group "a", :rvalue ##NaN}])))
