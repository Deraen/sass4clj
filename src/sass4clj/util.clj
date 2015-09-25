(ns sass4clj.util)

;;
;; Debugging
;; from boot.util
;;

; Hack to detect boot verbosity
(defn- get-verbosity []
  (try
    (require 'boot.util)
    ; Deref var and atom
    @@(resolve 'boot.util/*verbosity*)
    (catch Exception _
      1)))

(defn- print*
  [verbosity args]
  (when (>= (get-verbosity) verbosity)
    (binding [*out* *err*]
      (apply printf args) (flush))))

(defn dbug [& more] (print* 2 more))
(defn info [& more] (print* 1 more))
(defn warn [& more] (print* 1 more))
(defn fail [& more] (print* 1 more))
