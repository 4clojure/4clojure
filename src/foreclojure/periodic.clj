(ns foreclojure.periodic
  (:import (java.util.concurrent ScheduledThreadPoolExecutor
                                 ScheduledExecutorService
                                 TimeUnit)))

;; Stolen from clojail, but I wrote it myself anyway.
;; I guess it deserves a library of its own? Or maybe in useful?
(def uglify-time-unit
  (into {} (for [[enum aliases] {TimeUnit/NANOSECONDS [:ns :nanoseconds]
                                 TimeUnit/MICROSECONDS [:us :microseconds]
                                 TimeUnit/MILLISECONDS [:ms :milliseconds]
                                 TimeUnit/SECONDS [:s :sec :seconds]
                                 TimeUnit/MINUTES [:m :min :minutes]
                                 TimeUnit/HOURS [:h :hours]
                                 TimeUnit/DAYS [:d :days]}
                 alias aliases]
             {alias enum})))

(def ^ScheduledExecutorService pool
  (memoize (fn []
             (ScheduledThreadPoolExecutor. 2))))

(defn schedule-task [task period unit]
  (.scheduleAtFixedRate (pool) task 0
                        period (uglify-time-unit unit)))
