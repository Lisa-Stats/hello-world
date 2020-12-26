(ns hello
  (:require
   [io.pedestal.http :as http] ;;lets us start server
   [io.pedestal.http.route :as route])) ;;lets us create routes

(defn respond-hello [_request]
  {:status 200 :body "Hello, world!"})

(defn respond-home [_request]
  {:status 200 :body "Welcome home!"})

(defn respond-salutations [_request]
  {:status 200 :body "How are you?"})

;; when an http request comes in you will always have
;; your route, it will be the url followed by whatever
;; it is
;; when that route gets triggered in the server, it is
;; going to try to find a route that looks like that, if
;; there is one, then what do we do?
;; the what do we do part is the handler, it handles the
;; requests, it is the same idea as a function
;; when this route fires, that function is going to
;; handle that request

(def routes
  (route/expand-routes
   #{["/salutations" :get respond-salutations :route-name :sal]
     ["/" :get respond-home :route-name :home]
     ["/greet" :get respond-hello :route-name :greet]}))
;;we see the home url ("/") we point it to this function
;;respond-home
;;we see this thing called greet we point it to this
;;function respond-hello

;;double colon is a namespaced keyword
;;namespaced keyword is a way to organize workspaces
(defn create-server []
  (http/create-server
   {::http/routes routes ;;these are the routes I want active
    ::http/type :jetty
    ::http/port 8890}))

(defn start []
  (http/start (create-server)))
