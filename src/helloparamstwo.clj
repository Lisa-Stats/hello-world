(ns helloparamstwo
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]))

(defn respond-hello [_request]
  {:status 200 :body "Hi"})

(defn okay [okay]
  {:status 200 :body okay})

(defn not-okay []
  {:status 400 :body "400 bad request"})

(defn greeting-for [nm]
  (if (empty? nm)
    nil
    (str "Hello, " nm "!")))

(defn respond-param [request]
  (let [nm (get-in request [:query-params :name] "world")
        resp (greeting-for nm)]
    (if resp
      (okay resp)
      (not-okay))))

(def routes
  (route/expand-routes
   #{["/" :get respond-hello :route-name :hello]
     ["/greet" :get respond-param :route-name :greet]}))

;;first way to start server
  (defn create-server []
    (http/create-server
     {::http/routes routes
      ::http/type :jetty
      ::http/port 8080}))

(defn start []
  (http/start (create-server)))

;;second way to start server
(def service-map
  {::http/routes routes
   ::http/type :jetty
   ::http/port 8080})

(defn start-two []
  (http/start (http/create-server service-map)))