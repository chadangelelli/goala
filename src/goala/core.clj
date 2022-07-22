(ns goala.core
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :refer (pprint)]

    [integrant.core :as ig]
    [taoensso.timbre :as log]
    [xtdb.api :as xtdb]
    [aleph.http :as aleph]
    )
  (:gen-class))

;; __________________________________________________________________ DB
(defn rocksdb-kv-store
  [dir]
  {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
              :db-dir (io/file dir)
              :sync? true}})

(defmethod ig/init-key :db/node [_ config]
  (log/info (str "[GOALA] => Starting DB node" ))
  (xtdb/start-node config))

(defmethod ig/halt-key! :db/node [_ node]
  (log/info (str "[GOALA] => Stopping DB node" ))
  (.close node))

;; __________________________________________________________________ HTTP

(defmethod ig/init-key :http/server [_ opts]
  (let [handler (atom (delay (:handler opts)))
        options (dissoc opts :handler)]
    (log/info (str "[GOALA] => Starting HTTP server on port " (:port options)))
    {:handler handler
     :server  (aleph/start-server (fn [req] (@@handler req)) options)}))

(defmethod ig/halt-key! :http/server [_ {:keys [server]}]
  (log/info (str "[GOALA] => Stopping HTTP server"))
  (.close ^java.io.Closeable server))

(defmethod ig/suspend-key! :http/server [_ {:keys [handler]}]
  (log/info (str "[GOALA] => Suspending HTTP server"))
  (reset! handler (promise)))

(defmethod ig/resume-key :http/server [k opts old-opts old-impl]
  (log/info (str "[GOALA] => Resuming HTTP server on port " (:port opts)))
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
        old-impl)
    (do (ig/halt-key! k old-impl)
        (ig/init-key k opts))))

(defmethod ig/init-key :http/handler [_ _]
  (fn
    [req]
    {:status 200
     :body (with-out-str (pprint req))}))

;; __________________________________________________________________ SYSTEM

(def config (atom nil))
(def system (atom nil))

(defn start-app 
  []
  (reset! config (ig/read-string (slurp "config.edn")))
  (reset! system (ig/init @config))
  :started)

(defn stop-app
  []
  (reset! system (ig/halt! @system))
  :stopped)

(defn restart-app
  []
  (stop-app)
  (start-app)
  :restarted)
