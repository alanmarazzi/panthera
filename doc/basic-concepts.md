# Basic panthera concepts

This is an introductory guide to the concepts driving panthera 
and its usage.

## Series

*Serieses* are like vectors that act also as columns for *data-frames*. One *series* must have all the contained data with the same data type and if there is more than one type when you create a *series* than this one takes the most relaxed one.

```clojure
; This is how you create a series
(require '[panthera.panthera :as pt])

(println (pt/series [1 2 3]))
; 0    1
; 1    2
; 2    3
; dtype: int64
```

If we print the *series* we see on the left its index and on the right its values. As you can see below we get the underlying data type (*dtype*) as well. Let's swap 3 with "a" and see what happens.

```clojure
(println (pt/series [1 2 "a"]))
; 0    1
; 1    2
; 2    a
; dtype: object
```

Now the *dtype* it's become `object`, which in *panthera* means either `string` or something that can be represented with a `string` and is not a primitive.

If we get this data back to Clojure we'll see that we get the underlying original representation with mixed data types.

```clojure
(vec (pt/series [1 2 "a"]))
;[1 2 "a"]
```

This means that we can always from a representation to another without many problems. A *series* can be treated as a Clojure vector if we want to:

```clojure
(map inc (pt/series (range 3)))
;[1 2 3]
```

But when we do this we lose metadata tied to it. The difference with regular vectors is mostly this metadata:

- a *series* can have a name
- a *series* has a *dtype*
- a *series* has an index that can be freely named

Let's see a few examples:

```clojure
(pt/series {:name "my-series"})
; name    my-series
; dtype: object
```

We just created an empty *series* with the name "my-series" to show that it can exist even with just metadata. The map passes as an argument lets you add other options to the function call 

```clojure
(pt/series 1 {:name "my-series" :index "idx"})
; idx    1
; Name: my-series, dtype: int64
```