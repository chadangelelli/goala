(ns goala.http
  (:require 
    [taoensso.timbre :as log]
    [clojure.pprint :refer [pprint]]
    [integrant.core :as ig]
    [aleph.http :as aleph]
    [goala.util :as util]))

(defn start
  [opts]
  
  (let [handler (atom (delay (:handler opts)))
        options (dissoc opts :handler)]
    
    (util/log-info (str "Starting HTTP server on port " (:port options)))
    
    {:handler handler
     :server (aleph/start-server (fn [req] (@@handler req)) options)}))

(defn stop
  [{:keys [server]}]
  
  (util/log-info "Stopping HTTP server")
  
  (.close ^java.io.Closeable server))

(defn suspend 
  [{:keys [handler]}]
  
  (util/log-info "Suspending HTTP server")
  
  (reset! handler (promise)))

(defn resume 
  [k opts old-opts old-impl]
  
  (util/log-info "Resuming HTTP server on port " (:port opts))
  
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
        old-impl)
    (do (ig/halt-key! k old-impl)
        (ig/init-key k opts))))

(defn handler
  [req]
  {:status 200
   :body (with-out-str (pprint req))})
