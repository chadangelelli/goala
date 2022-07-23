(ns goala.views
  (:require [hiccup.page :as page]))

(defn base-page
  [_ & contents]
  (page/html5
    [:head 
     [:title "Goala | <FIXME>"]
     [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
     [:link {:rel "preconnect" 
             :href "https://fonts.gstatic.com" 
             :crossorigin "crossorigin" }]
     [:link 
      {:rel="stylesheet"
       :href 
       (str 
         "https://fonts.googleapis.com/css2"
         "?family=Montserrat:wght@100;200;500"
         "&family=Open+Sans:ital,wght@0,300;0,400;0,500;0,700;1,300;1,700"
         "&display=swap")}]
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
