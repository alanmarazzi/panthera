(ns panthera.reshape-test
  (:require
   [clojure.test :refer :all]
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u :reload true]
   [panthera.pandas.generics :as g]
   [panthera.pandas.reshape :as r :reload true]
   [panthera.pandas.math :as m :reload true]
   [panthera.pandas.conversion :as c]))

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

(deftest merge-asof
  (let [trades (g/data-frame
                 {:time     (c/->datetime ["2016-05-25 13:30:00.023"
                                           "2016-05-25 13:30:00.038"
                                           "2016-05-25 13:30:00.048"
                                           "2016-05-25 13:30:00.048"])
                  :ticker   [:MSFT :MSFT :GOOG :AAPL]
                  :price    [51.95 51.95 720.77 98.00]
                  :quantity [75 155 100 100]})
        quotes (g/data-frame
                 {:time   (c/->datetime ["2016-05-25 13:30:00.023"
                                         "2016-05-25 13:30:00.023"
                                         "2016-05-25 13:30:00.030"
                                         "2016-05-25 13:30:00.048"
                                         "2016-05-25 13:30:00.049"])
                  :ticker [:GOOG :MSFT :MSFT :GOOG :AAPL]
                  :bid    [720.5 51.95 51.97 720.5 97.99]
                  :ask    [720.93 51.96 51.98 720.93 98.01]})]
    (are [d o]
        (m/same? (r/merge-asof trades quotes d) (g/data-frame o))
      {:on       :time
       :suffixes [:-x :-y]} [{:time     (c/->datetime "2016-05-25 13:30:00.023000"),
                              :ticker-x "MSFT",
                              :price    51.95,
                              :quantity 75,
                              :ticker-y "MSFT",
                              :bid      51.95,
                              :ask      51.96}
                             {:time     (c/->datetime "2016-05-25 13:30:00.038000"),
                              :ticker-x "MSFT",
                              :price    51.95,
                              :quantity 155,
                              :ticker-y "MSFT",
                              :bid      51.97,
                              :ask      51.98}
                             {:time     (c/->datetime "2016-05-25 13:30:00.048000"),
                              :ticker-x "GOOG",
                              :price    720.77,
                              :quantity 100,
                              :ticker-y "GOOG",
                              :bid      720.5,
                              :ask      720.93}
                             {:time     (c/->datetime "2016-05-25 13:30:00.048000"),
                              :ticker-x "AAPL",
                              :price    98.0,
                              :quantity 100,
                              :ticker-y "GOOG",
                              :bid      720.5,
                              :ask      720.93}]

      {:on                  :time
       :allow-exact-matches false
       :suffixes            [:-x :-y]} [{:time     (c/->datetime "2016-05-25 13:30:00.023000"),
                                         :ticker-x "MSFT",
                                         :price    51.95,
                                         :quantity 75,
                                         :ticker-y ##NaN,
                                         :bid      ##NaN,
                                         :ask      ##NaN}
                                        {:time     (c/->datetime "2016-05-25 13:30:00.038000"),
                                         :ticker-x "MSFT",
                                         :price    51.95,
                                         :quantity 155,
                                         :ticker-y "MSFT",
                                         :bid      51.97,
                                         :ask      51.98}
                                        {:time     (c/->datetime "2016-05-25 13:30:00.048000"),
                                         :ticker-x "GOOG",
                                         :price    720.77,
                                         :quantity 100,
                                         :ticker-y "MSFT",
                                         :bid      51.97,
                                         :ask      51.98}
                                        {:time     (c/->datetime "2016-05-25 13:30:00.048000"),
                                         :ticker-x "AAPL",
                                         :price    98.0,
                                         :quantity 100,
                                         :ticker-y "MSFT",
                                         :bid      51.97,
                                         :ask      51.98}]

      {:on        :time
       :direction :forward
       :suffixes  [:-x :-y]} [{:time     (c/->datetime "2016-05-25 13:30:00.023000"),
                               :ticker-x "MSFT",
                               :price    51.95,
                               :quantity 75,
                               :ticker-y "GOOG",
                               :bid      720.5,
                               :ask      720.93}
                              {:time     (c/->datetime "2016-05-25 13:30:00.038000"),
                               :ticker-x "MSFT",
                               :price    51.95,
                               :quantity 155,
                               :ticker-y "GOOG",
                               :bid      720.5,
                               :ask      720.93}
                              {:time     (c/->datetime "2016-05-25 13:30:00.048000"),
                               :ticker-x "GOOG",
                               :price    720.77,
                               :quantity 100,
                               :ticker-y "GOOG",
                               :bid      720.5,
                               :ask      720.93}
                              {:time     (c/->datetime "2016-05-25 13:30:00.048000"),
                               :ticker-x "AAPL",
                               :price    98.0,
                               :quantity 100,
                               :ticker-y "GOOG",
                               :bid      720.5,
                               :ask      720.93}]
      {:on       :time
       :by       :ticker
       :suffixes [:-x :-y]}  [{:time     (c/->datetime "2016-05-25 13:30:00.023000"),
                               :ticker   "MSFT",
                               :price    51.95,
                               :quantity 75,
                               :bid      51.95,
                               :ask      51.96}
                              {:time     (c/->datetime "2016-05-25 13:30:00.038000"),
                               :ticker   "MSFT",
                               :price    51.95,
                               :quantity 155,
                               :bid      51.97,
                               :ask      51.98}
                              {:time     (c/->datetime "2016-05-25 13:30:00.048000"),
                               :ticker   "GOOG",
                               :price    720.77,
                               :quantity 100,
                               :bid      720.5,
                               :ask      720.93}
                              {:time     (c/->datetime "2016-05-25 13:30:00.048000"),
                               :ticker   "AAPL",
                               :price    98.0,
                               :quantity 100,
                               :bid      ##NaN,
                               :ask      ##NaN}])))

(deftest concatenate
  (are [d o]
      (m/same?
        (r/concatenate [(g/data-frame {:a [1 2 3]
                                       :b [4 5 6]})
                        (g/data-frame {:a [2 2 2]
                                       :b [3 3 3]})] d)
        (g/data-frame o))

    {} [{:a 1, :b 4} {:a 2, :b 5} {:a 3, :b 6}
        {:a 2, :b 3} {:a 2, :b 3} {:a 2, :b 3}]

    {:axis 1} [{:a 2, :b 3} {:a 2, :b 3} {:a 2, :b 3}]

    {:axis 1
     :ignore-index true} [{0 1, 1 4, 2 2, 3 3}
                          {0 2, 1 5, 2 2, 3 3}
                          {0 3, 1 6, 2 2, 3 3}]))
