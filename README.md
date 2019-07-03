# panthera

> ## Hic sunt leones

Latin phrase reported on many maps indicating *Terra incognita*, unexplored or harsh land.

## What

Dataframes in Clojure. Through [pandas](https://github.com/pandas-dev/pandas). On Python.

## How

```clojure
(require '[panthera.panthera :as pt])

(-> (read-csv "mycsv.csv")
    (subset-cols "Col1" "Col2" "Col3")
    median)
```

## License

Copyright © 2019 Alan Marazzi

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.