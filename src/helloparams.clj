(ns helloparams
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [clojure.string :as strings]))

;;conditional responses
(defn ok [body]
  {:status 200 :body body})

(defn not-found []
  {:status 404 :body "Not found\n"})

(def unmentionables
  #{"yhwh" "voldemort" "mxyzptlk" "rumplestiltskin"})

(defn lower [nm]
  (strings/lower-case nm))

(defn greeting-for [nm]
  (cond
    (unmentionables (lower nm))  nil
    (empty? nm)  "Hello, world!"
    :else  (str "Hello, " nm "\n")))

(defn respond-hello [request]
  (let [nm (get-in request [:query-params :name])
        resp (greeting-for nm)]
    (if resp
      (ok resp)
      (not-found))))

(def routes
  (route/expand-routes
   #{["/greet" :get respond-hello :route-name :greet]}))

(def service-map
  {::http/routes routes
   ::http/type :jetty
   ::http/port 8890})

(defn start []
  (http/start (http/create-server service-map)))

;;defonce means we can recompile this file in the same
;;REPL w/o overwriting the value in this atom |
;;every piece of data in clojure is immutable,
;;atoms give you the ability to mutate something and can
;;make an atom anything, any clojure var
;;except when you want to change it, call swap! or reset!
;;to change the value
(defonce server (atom nil))

;;reset! replaces the current value in the atom (nil)
;;with the value of the entire server 
;;this server is the actual atom
(defn start-dev []
  (reset! server
          (http/start (http/create-server
                       (assoc service-map
                              ::http/join? false)))))

;;when you want to use the value inside of the atom
;;need to deref it
;;that's why the @ is there |
;;the value inside the server atom, it is the
;;dereferenced value
;;give me the value that is inside this atom right now
(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))

;;basic using the param
;;  (defn respond-hello [request]
  ;;(let [nm (get-in request [:query-params :name])]
    ;;{:status 200 :body (str "Hello, " nm "\n")}))

;;basic with answer for bad user input
;;(defn respond-hello [request]
  ;;(let [nm (get-in request [:query-params :name])
    ;;    resp (if (empty? nm)
      ;;         "Hello, world!\n"
        ;;       (str "Hello, " nm "\n"))]
    ;;{:status 200 :body resp}))

;;refactoring of respond-hello from line 69,
;;b/c it mixes too many concerns
;;better to separate those concerns - easier to test

;;(defn ok [body]
  ;;{:status 200 :body body})

;;(defn greeting-for [nm]
  ;;(if (empty? nm)
    ;;"Hello world!\n"
    ;;(str "Hello, " nm "\n")))

;;(defn respond-hello [request]
  ;;(let [nm (get-in request [:query-params :name])
    ;;    resp (greeting-for nm)]
    ;;(ok resp)))

(comment
  ;;this is a request map
  ;;describes the incoming HTTP request
  ;;taking a look at request by echoing it back to the
  ;;client -
  ;;(defn respond-hello [request]
  ;;{:status 200 :body request})
  ;;common debugging trick
  ;;access its values with get and get-in
  {:protocol "HTTP/1.1"
   :async-supported? true
   :remote-addr "127.0.0.1"
   :path-info "/greet"
   :uri "/greet"
   :query-string "name=Michael"
   :query-params {:name "Michael"}
   :params {:name "Michael"}
   :headers {"user-agent" "curl/7.68.0", "accept" "*/*", "host" "localhost:8890"}
   :server-port 8890
   :server-name "localhost"
   :path-params []
   :scheme :http
   :request-method :get})