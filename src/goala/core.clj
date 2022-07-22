(ns goala.core
  (:require
    [integrant.core :as ig]
    [goala.db :as db]
    [goala.http :as http]))

(defmethod ig/init-key     :db/node      [_ cnf]  (db/start cnf))
(defmethod ig/halt-key!    :db/node      [_ node] (db/stop node))
(defmethod ig/init-key     :http/server  [_ opts] (http/start opts))
(defmethod ig/halt-key!    :http/server  [_ cnf]  (http/stop cnf))
(defmethod ig/suspend-key! :http/server  [_ cnf]  (http/suspend cnf))
(defmethod ig/init-key     :http/handler [_ _]    http/handler)

(defmethod ig/resume-key :http/server [k opts old-opts old-impl]
  (http/resume k opts old-opts old-impl))

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
