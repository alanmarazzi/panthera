(ns panthera.pandas.conversion
  (:require
   [libpython-clj.python :as py]
   [panthera.pandas.utils :as u]))

(defn ->numeric
  "Coerce the given series to numeric.

  **Arguments**

  - `seq-or-srs` -> series, Iterable

  **Attrs**

  - `:errors` -> {\"ignore\" \"raise\" \"coerce\"}, default \"raise\":
  If \"raise\" invalid parsing raises a Python exception, if \"coerce\"
  invalid parsing gets set as NaN and if \"ignore\" invalid parsing
  returns the input
  - `:downcast` -> {\"integer\" \"signed\" \"unsigned\" \"float\"}, default nil
  If not nil and if the data has been succesfully cast downcast the resulting
  data to the smallest numerical dtype possible according to the given rule

  **Examples**

  ```
  (->numeric [:1 :2 :3])
  ; [1 2 3]

  (->numeric (series [\"1\" 2 :3]))
  ; [1 2 3]
  ```"
  [seq-or-srs & [attrs]]
  (u/kw-call u/pd "to_numeric" seq-or-srs attrs))

(defn ->datetime
  "Convert to datetime

  **Arguments**

  - `seq-or-srs` -> series, Iterable

  **Attrs**

  - `:errors` -> {\"ignore\" \"raise\" \"coerce\"}, default \"raise\":
  If \"raise\" invalid parsing raises a Python exception, if \"coerce\"
  invalid parsing gets set as NaN and if \"ignore\" invalid parsing
  returns the input
  - `:dayfirst` -> bool, default false: if true parse dates with day first
  - `:yearfirst` -> bool, default false: if true parse dates with year first.
  If both `:dayfirst` and `:yearfirst` are true the year comes first
  - `:utc` -> bool, default false: return UTC time if true
  - `:format` -> str: the format string to parse time. Check original docs for
  more info https://docs.python.org/3/library/datetime.html#strftime-and-strptime-behavior
  - `:exact` -> bool, default true: true requires an exact format match, otherwise
  match the format anywhere in the string
  - `:unit` -> {\"D\" \"s\" \"ms\" \"us\" \"ns\"}, default \"ns\":
  unit of the given offset time
  - `:infer-datetime-format` -> bool, default false: attempt to infer the format
  of the datetime. If it can be inferred automatically switches to the fastest
  way to parse them
  - `:origin` -> str, num, default \"unix\":
  If ‘unix’ (or POSIX) time; origin is set to 1970-01-01.
  If ‘julian’, unit must be ‘D’, and origin is set to beginning of Julian Calendar.
  Julian day number 0 is assigned to the day starting at noon on January 1, 4713 BC.
  If Timestamp convertible, origin is set to Timestamp identified by origin.
  - `:cache` -> bool, default true: if true cache converted dates

  **Examples**

  ```
  (->datetime 12300000000000)
  ; 1970-01-01 03:25:00

  (->datetime 123 {:unit :D})
  ; 1970-05-04 00:00:00

  (->datetime \"2019-01-01\")
  ; 2019-01-01 00:00:00
  ```"
  [seq-or-srs & [attrs]]
  (u/kw-call u/pd "to_datetime" seq-or-srs attrs))

(defn ->timedelta
  "Convert to timedelta: a timedelta is an absolute difference in times

  **Arguments**

  - `seq-or-srs` -> series, Iterable

  **Attrs**

  - `:unit` -> {\"Y\" \"M\" \"W\" \"D\" \"days\" \"day\" \"hours\" \"hour\" \"hr\"
  \"h\" \"m\" \"minute\" \"min\" \"minutes\" \"T\" \"S\" \"seconds\" \"sec\" \"second\"
  \"ms\" \"milliseconds\" \"millisecond\" \"milli\" \"millis\" \"L\" \"us\"
  \"microseconds\" \"microsecond\" \"micro\" \"micros\" \"U\" \"ns\" \"nanoseconds\"
  \"nano\" \"nanos\" \"nanosecond\" \"N\"}, default \"ns\": the unit of the
  timedelta
  - `:errors` -> {\"ignore\" \"raise\" \"coerce\"}, default \"raise\":
  If \"raise\" invalid parsing raises a Python exception, if \"coerce\"
  invalid parsing gets set as NaN and if \"ignore\" invalid parsing
  returns the input

  **Examples**

  ```
  (->timedelta 123 {:unit :h})
  ; 5 days 03:00:00
  ```
  "
  [seq-or-srs & [attrs]]
  (u/kw-call u/pd "to_timedelta" seq-or-srs attrs))

;; bdate_range and period_range are just date_range with freq defaults
(defn date-range
  "Works as `range` but with dates

  **Attrs**

  - `:start` -> str: left bound for the dates
  - `:end` -> str: right bound
  - `:periods` -> int: number of periods to generate
  - `:freq` -> str, default \"D\": the frequency of the offset, they can have
  multiples: \"5D\". Check [original docs](https://pandas.pydata.org/pandas-docs/stable/user_guide/timeseries.html#timeseries-offset-aliases) for all possible frequencies
  - `:tz` -> str: time zone, for example \"Asia/Hong_Kong\"
  - `:normalize` -> bool, default false: normalize start/end dates to midnight
  - `:name` -> str: name of the resulting series
  - `:closed` -> {\"left\" \"right\"}, default nil: make the interval closed, if
  nil is closed on both sides

  **Examples**

  ```
  (date-range {:start \"2019-01-01\" :periods 3})
  ; DatetimeIndex(['2019-01-01', '2019-01-02', '2019-01-03'], dtype='datetime64[ns]', freq='D')
  ```"
  [& [attrs]]
  (u/simple-kw-call u/pd "date_range" attrs))

(defn timedelta-range
  "Works as [[date-range]] but with timedeltas

  **Attrs**

  - `:start` -> str: left bound for timedeltas
  - `:end` -> str: right bound
  - `:periods` -> int: number of periods to generate
  - `:freq` -> str, default \"D\": the frequency of the offset, they can have
  multiples: \"5D\". Check [original docs](https://pandas.pydata.org/pandas-docs/stable/user_guide/timeseries.html#timeseries-offset-aliases) for all possible frequencies
  - `:name` -> str: name of the resulting series
  - `:closed` -> {\"left\" \"right\"}, default nil: make the interval closed, if
  nil is closed on both sides

  **Examples**

  ```
  (timedelta-range {:start \"1 day\" :periods 3})
  ; TimedeltaIndex(['1 days', '2 days', '3 days'], dtype='timedelta64[ns]', freq='D')
  ```"
  [& [attrs]]
  (u/simple-kw-call u/pd "timedelta_range" attrs))

(defn interval-range
  "Return a range of intervals

  **Attrs**

  - `:start` -> str: left bound for intervals
  - `:end` -> str: right bound
  - `:periods` -> int: number of periods to generate
  - `:freq` -> str, numeric, default nil: the frequency of the offset, check
  [[date-range]] or [[timedelta-range]] for more info on time frequencies
  - `:name` -> str: name of the resulting series
  - `:closed` -> {\"left\" \"right\" \"both\" \"neither\"}, default \"right\":
  make the interval closed on the given side/s

  **Examples**

  ```
  (interval-range {:start 10 :periods 3 :freq 2})
  ; IntervalIndex([(10, 12], (12, 14], (14, 16]],
  ;             closed='right',
  ;             dtype='interval[int64]')
  ```"
  [& [attrs]]
  (u/simple-kw-call u/pd "interval_range" attrs))

(defn infer-time-freq
  "Try to infer the most likely frequency

  **Arguments**

  - `seq-or-srs` -> series, Iterable

  **Attrs**

  - `:warn` -> bool, default true: when true prints a warning if uncertain

  **Examples**

  ```
  (infer-time-freq [\"2017\" \"2018\" \"2019\"])
  ; \"AS-JAN\"
  ```"
  [seq-or-srs & [warn]]
  (u/kw-call u/pd "infer_freq" seq-or-srs {:warn (or warn true)}))

(defn astype
  "Cast the given data to a specific dtype

  **Arguments**

  - `df-or-srs` -> dataframe, series
  - `dtype` -> keyword, str, map: the specified dtype. For dataframes is possible
  to specify {:col :dtype}. Check [here](https://docs.scipy.org/doc/numpy/user/basics.types.html) for a full list
  of available dtypes

  **Attrs**

  - `:copy` -> bool, default true: generally leave this as true, otherwise values
  will be changed in place
  - `:errors` -> {\"raise\" \"ignore\"}, default \"raise\"

  **Examples**

  ```
  (astype (series [1 2 3]) :float32)
  ; 0    1.0
  ; 1    2.0
  ; 2    3.0
  ; dtype: float32
  ```"
  [df-or-srs dtype & [attrs]]
  (u/kw-call df-or-srs "astype" dtype attrs))
