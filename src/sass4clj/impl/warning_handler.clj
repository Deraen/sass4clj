(ns sass4clj.impl.warning-handler
  (:import [io.bit3.jsass.annotation WarnFunction])
  (:gen-class
    :name sass4clj.impl.WarningHandler
    :state state
    :init init
    :methods [[^{WarnFunction true} warn [String] void]
              [getWarnings [] Object]]))

(defn -init []
  [[] (atom [])])

;; TODO: Handle @debug also

(defn -warn [this message]
  (swap! (.state this) conj message))

(defn -getWarnings [this]
  @(.state this))

(comment
  (compile 'sass4clj.impl.warning-handler))
