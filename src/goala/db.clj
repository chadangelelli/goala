(ns goala.db 
  (:require 
    [xtdb.api :as xtdb]
    [goala.util :as util]))

(defn start
  [config]
  (util/log-info "Starting DB node")
  (xtdb/start-node config))

(defn stop
  [node]
  (util/log-info "Stopping DB node")
  (.close node))
