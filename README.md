# panthera

![panthera-logo](https://github.com/alanmarazzi/panthera/blob/master/resources/panthera.png)

> **Hic sunt leones**

Latin phrase reported on many maps indicating *Terra incognita*, unexplored or harsh land.

## What

Dataframes in Clojure. Through [pandas](https://github.com/pandas-dev/pandas). On Python.

## Disclaimer

This is alpha, things will change fast, will break and the API is neither complete, nor settled. Since a few people have started playing with this there's a Clojars project available. Please give feedback if you're using this, every kind of contribution is appreciated (for more info check the [Contributing](#contributing) section). At the moment everything is mostly undocumented and untested, I'm currently adding them.

[![Clojars Project](https://img.shields.io/clojars/v/panthera.svg)](https://clojars.org/panthera)

## Get started

**Panthera** uses the great [libpython-clj](https://github.com/cnuernber/libpython-clj) as a backend to access Python and get [pandas](https://github.com/pandas-dev/pandas) and [numpy](https://github.com/numpy/numpy) functionality.

### N.B.: check [libpython-clj](https://github.com/cnuernber/libpython-clj) repo on how to install and start a Clojure/Python session.

### The actual code

After this you can start playing around with **panthera**

```clojure
(require '[[panthera.panthera :as pt]
           [libpython-clj.python :refer [initialize!]])
           
(initialize!)

(-> (pt/read-csv "mycsv.csv")
    (pt/subset-cols "Col1" "Col2" "Col3")
    pt/median)
```

The above chain will read your csv file as a DataFrame, select only the given columns and then return a Series with the median of each column.

`panthera.panthera` is the home of the main API, and you can find everything there. The advice is to never `:use` or `:refer :all` the namespace because there are some functions named as core Clojure functions such as `mod` which in this case does the same thing as the core one, but in this case it is vectorized and it works only if the first argument is a Python object.

## Numpy

All of Numpy is accessible through [libpython-clj](https://github.com/cnuernber/libpython-clj) interop, check the repo for more info.

## Contributing

Please let me know about any issues, quirks, ideas or even just to say that you're doing something cool with this! I accept issues, PRs or direct messages (you can find me also on https://clojurians.slack.com and on https://clojurians.zulipchat.com).

## Examples

You can find some examples in the [examples](https://github.com/alanmarazzi/panthera/tree/master/examples) folder. At the moment that's the best way to start with panthera.

- [panthera intro](https://github.com/alanmarazzi/panthera/blob/master/examples/panthera-intro.ipynb) ([nbviewer](https://nbviewer.jupyter.org/github/alanmarazzi/panthera/blob/master/examples/panthera-intro.ipynb))
- [basic concepts (serieses & data-frames)](https://github.com/alanmarazzi/panthera/blob/master/examples/basic-concepts.ipynb) ([nbviewer](https://nbviewer.jupyter.org/github/alanmarazzi/panthera/blob/master/examples/basic-concepts.ipynb))
- [general Python package wrapper](https://github.com/alanmarazzi/panthera/blob/master/src/panthera/numpy.clj#L84) - an example about how to use panthera to wrap other Python libraries

## Why "panthera"?

Pandas is derived from "panel data" and somehow is supposed to mean "Python data analysis library" as well. Though it shouldn't have nothing to do with the cute Chinese bears, there are [logos showing a bear](https://michaelsaruggia.com/wp-content/uploads/2019/03/pandas-python.jpg).

Panthera doesn't pretend to be a clever wordplay because it doesn't need to. First off [panthera is latin](https://en.wiktionary.org/wiki/panthera) and it literally means "large cat", second though pandas are surely cute, pantherae are way cooler (and [snow leopards](https://en.wikipedia.org/wiki/Snow_leopard) also happen to be among the very few predators of pandas, but that's just a case...).

## Special thanks

- [libpython-clj](https://github.com/cnuernber/libpython-clj)
- [pandas](https://pandas.pydata.org/)
- [numpy](https://www.numpy.org/)
- [clojure](https://clojure.org/)
- [logo](https://www.vecteezy.com)

## License

Copyright © 2020 Alan Marazzi

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.