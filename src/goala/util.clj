(ns goala.util
  (:require
    [taoensso.timbre :as log]))

(defn log-info
  [s]
  (log/info (str "[GOALA] => " s)))

