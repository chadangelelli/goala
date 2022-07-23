(ns goala.http
  (:require 
    [integrant.core :as ig]
    [aleph.http :as aleph]
    [buddy.auth :as buddy-auth]
    [buddy.auth.backends :as buddy-auth-backends]
    [buddy.auth.backends.httpbasic :as buddy-auth-backends-httpbasic]
    [buddy.auth.middleware :as buddy-auth-middleware]
    [buddy.hashers :as buddy-hashers]
    [buddy.sign.jwt :as jwt]
    [muuntaja.core :as m]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [ring.middleware.params :as params]

    [goala.views :as views]
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

(def db
  "We use a simple map as a db here but in real-world you would
  interface with a real data storage in `basic-auth` function."
  {"user1"
   {:id       1
    :password (buddy-hashers/encrypt "kissa13")
    :roles    ["admin" "user"]}
   "user2"
   {:id       2
    :password (buddy-hashers/encrypt "koira12")
    :roles    ["user"]}})

(def private-key
  "Used for signing and verifying JWT-tokens In real world you'd read
  this from an environment variable or some other configuration that's
  not included in the source code."
  "kana15")

(defn create-token
  "Creates a signed jwt-token with user data as payload.
  `valid-seconds` sets the expiration span."
  [user & {:keys [valid-seconds] :or {valid-seconds 7200}}] ;; 2 hours
  (let [payload (-> user
                    (select-keys [:id :roles])
                    (assoc :exp (.plusSeconds
                                 (java.time.Instant/now) valid-seconds)))]
    (jwt/sign payload private-key {:alg :hs512})))

(def token-backend
  "Backend for verifying JWT-tokens."
  (buddy-auth-backends/jws {:secret private-key :options {:alg :hs512}}))

(defn basic-auth
  "Authentication function called from basic-auth middleware for each
  request. The result of this function will be added to the request
  under key :identity.
  NOTE: Use HTTP Basic authentication always with HTTPS in real setups."
  [db request {:keys [username password]}]
  (let [user (get db username)]
    (if (and user (buddy-hashers/check password (:password user)))
      (-> user
          (dissoc :password)
          (assoc :token (create-token user)))
      false)))

(defn create-basic-auth-backend
  "Creates basic-auth backend to be used by basic-auth-middleware."
  [db]
  (buddy-auth-backends-httpbasic/http-basic-backend
   {:authfn (partial basic-auth db)}))

(defn create-basic-auth-middleware
  "Creates a middleware that authenticates requests using http-basic
  authentication."
  [db]
  (let [backend (create-basic-auth-backend db)]
    (fn [handler]
      (buddy-auth-middleware/wrap-authentication handler backend))))

(defn token-auth-middleware
  "Middleware used on routes requiring token authentication."
  [handler]
  (buddy-auth-middleware/wrap-authentication handler token-backend))

(defn admin-middleware
  "Middleware used on routes requiring :admin role."
  [handler]
  (fn [request]
    (if (-> request :identity :roles set (contains? "admin"))
      (handler request)
      {:status 403 :body {:error "Admin role required"}})))

(defn auth-middleware
  "Middleware used in routes that require authentication. 
  If request is not authenticated a 301 redirect to /login is returned. 
  Buddy checks if request key :identity is set to truthy value 
  by any previous middleware."
  [handler]
  (fn [request]
    (if (buddy-auth/authenticated? request)
      (handler request)
      {:status 301 :headers {"Location" "/login"} :body ""})))

(def routes
  [["/public/*" (ring/create-resource-handler)]
   ["/login"    {:get views/login-page}]
   ["/"         {:get views/home-page
                 :middleware [(create-basic-auth-middleware db) 
                              auth-middleware]}]


   ["/basic-auth"
    [""
     {:middleware [(create-basic-auth-middleware db) 
                   auth-middleware]
      :get
      (fn [req]
        {:status 200
         :body
         {:message "Basic auth succeeded!"
          :user    (-> req :identity)}})}]]

   ["/token-auth"
    [""
     {:middleware [token-auth-middleware 
                   auth-middleware]
      :get        (fn [_] {:status 200 :body {:message "Token auth succeeded!"}})}]]

   ["/token-auth-with-admin-role"
    [""
     {:middleware [token-auth-middleware
                   auth-middleware
                   admin-middleware]
      :get        (fn [_] {:status 200 :body {:message "Token auth with admin role succeeded!"}})}]]])

(def handler
  (ring/ring-handler
    (ring/router
      routes
      {:data
       {:muuntaja m/instance
        :middleware ; applied to all routes
        [params/wrap-params
         muuntaja/format-middleware
         coercion/coerce-exceptions-middleware
         coercion/coerce-request-middleware
         coercion/coerce-response-middleware]}})
    (ring/create-default-handler)))
