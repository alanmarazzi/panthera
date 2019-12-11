# panthera

![panthera-logo](https://github.com/alanmarazzi/panthera/blob/master/resources/panthera.png)

> **Hic sunt leones**

Latin phrase reported on many maps indicating *Terra incognita*, unexplored or harsh land.

## What

Dataframes in Clojure. Through [pandas](https://github.com/pandas-dev/pandas). On Python.

## Disclaimer

This is very alpha, things will change fast, will break and the API is neither complete, nor settled. Since a few people have started playing with this there's a Clojars project available. Please give feedback if you're using this, every kind of contribution is appreciated (for more info check the [Contributing](#contributing) section). At the moment everything is mostly undocumented and untested, I'm currently adding them.

[![Clojars Project](https://img.shields.io/clojars/v/panthera.svg)](https://clojars.org/panthera)

## Get started

**Panthera** uses the great [libpython-clj](https://github.com/cnuernber/libpython-clj) as a backend to access Python and get [pandas](https://github.com/pandas-dev/pandas) and [numpy](https://github.com/numpy/numpy) functionality.

### System level

If you usually don't develop in Python then a system level install might be a good solution (though always discouraged), if this is your case then follow the subsequent steps.

To get started you need python, pandas and numpy (the latter comes with the former) on your path. Usually a:

```bash
sudo apt install libpython3.6-dev
pip3 install numpy pandas xlrd # the latter is for Excel files, if you don't care you can do without
```

### Environments

If you want to have different Python environments, then getting **panthera** to work correctly is a bit more tricky.

First create your new environment with at least python=3.6, numpy and pandas. (This was tested both on GNU/Linux and WSL with [conda](https://docs.conda.io/projects/conda/en/latest/), but there's no reason why it shouldn't work with other env management tools. On other systems, [Docker is your best bet](https://github.com/scicloj/docker-hub/tree/master/panthera)):

```bash
conda create -n panthera python=3.6 numpy pandas
```

Then check the path to the newly created environment:

```bash
conda activate panthera
which python
```

Now you just have to add to one of your profiles the path to the wanted python executable:

```bash
{:dev {:resource-paths ["/home/user/miniconda3/envs/panthera"]}}
```

You can create different profiles with different paths according to what you need. Now if you want to make it possible to work with **panthera** without having to activate your environments you have 2 choices:

- assign `PYTHONHOME` env variable to your environment

```bash
PYTHONHOME="/home/user/miniconda3/envs/panthera" lein whatever
```

- assign `PYTHONHOME` env variable before requiring **panthera**

```bash
(System/setProperty "PYTHONHOME" "/home/user/miniconda3/envs/panthera")
```

### The actual code

After this you can start playing around with **panthera**

```clojure
(require '[panthera.panthera :as pt])

(-> (pt/read-csv "mycsv.csv")
    (pt/subset-cols "Col1" "Col2" "Col3")
    pt/median)
```

The above chain will read your csv file as a DataFrame, select only the given columns and then return a Series with the median of each column.

`panthera.panthera` is the home of the main API, and you can find everything there. The advice is to never `:use` or `:refer :all` the namespace because there are some functions named as core Clojure functions such as `mod` which in this case does the same thing as the core one, but in this case it is vectorized and it works only if the first argument is a Python object.

## Numpy

All of Numpy is wrapped and accessible through a single interface from `panthera.numpy`.

```clojure
(require '[panthera.numpy :refer [npy doc]])

(npy :power {:args [[1 2 3] 3]})
;=> [1 8 27]

(npy :power)
; This arity returns the actual numpy object that can be passed around to other functions as an argument
```

To access functions inside submodules pass to `npy` a sequence of keys leading to the wanted function:

```clojure
(npy [:linalg :svd] {:args [[1 2 3] [4 5 6]]})
```

You can check the original docstring for every module and function with the `doc` helper

```clojure
(doc :power)

(doc [:linalg :eigh])
```

To see what is available and how everything works check the [official docs](https://docs.scipy.org/doc/numpy/reference/) online.

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

Copyright © 2019 Alan Marazzi

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.
