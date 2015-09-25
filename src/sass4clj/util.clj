(ns sass4clj.util)

;;
;; Debugging
;; from boot.util
;;

(def ^:dynamic *verbosity* 1)

(defn- print*
  [verbosity args]
  (when (>= *verbosity* verbosity)
    (binding [*out* *err*]
      (apply printf args) (flush))))

(defn dbug [& more] (print* 2 more))
(defn info [& more] (print* 1 more))
(defn warn [& more] (print* 1 more))
(defn fail [& more] (print* 1 more))
