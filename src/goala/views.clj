(ns goala.views
  (:require [hiccup.page :as page]))

(defn base-page
  [_ & contents]
  (page/html5
    [:head 
     [:title "Goala | <FIXME>"]
     (page/include-css "/public/css/styles.css")]
    (into [:body] contents)
    (page/include-js "/public/js/app.js")))

(defn login-page
  [request]
  {:status 200
   :body 
   (base-page
     request
     [:div
      [:h1 "Login Page"]])})

(defn home-page
  [request]
  {:stuat 200
   :body
   (base-page
     request
     [:div
      [:h1 "Home Page"]])})
