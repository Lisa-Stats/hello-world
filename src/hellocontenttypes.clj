(ns hellocontenttypes
  (:require
   [clojure.data.json :as json]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [io.pedestal.http.content-negotiation :as conneg]
   [clojure.pprint :as pp]))

(defn ok [body]
  {:body body
   :status 200})

(defn not-found []
  {:status 404 :body "Not found\n"})

;;(def echo
  ;;{:name ::echo
   ;;:enter #(assoc % :response (ok (:request %)))})

;;old echo function
;;just dealing with a clojure hash map
(def echo ;;name it a namespaced qualified keyword echo
  {:name ::echo ;;so it will know it comes from hellocontenttypes namespace
   :enter (fn [context] ;;where value of enter is just a function
            (let [request (:request context) ;;1 arg - context hashmap
                                           ;;pull out request keyword
                 response (ok request)] ;;make a response map out of it
              (clojure.pprint/pprint (assoc context :response response))))})
;;associate context map with response keyword and give it
;;the value, response |
;;assoc returns the entire context map, with the response
;;attached to it
;;normally would not write this in such an expanded form
;;this is at the interceptor level and not with a route like before

(def unmentionables
  #{"yhwh" "voldemort" "mxyzptlk" "rumplestiltskin"})

(defn greeting-for [nm]
  (cond
    (unmentionables nm)  nil
    (empty? nm)  "Hello, world!\n"
    :else  (str "Hello, " nm "\n")))

(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])

(def content-neg-intc (conneg/negotiate-content supported-types))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/html"        body
    "text/plain"       body
    "application/edn"  (pr-str body)
    "application/json" (json/write-str body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (if (get-in context [:response :headers "Content-Type"])
       context
       (update-in context [:response] coerce-to (accepted-type context))))})
;;OR
;;using cond ->
;;(def coerce-body
;;{:name ::coerce-body
;;:leave
;;(fn [context]
;;(cond -> context
;;(nil? (get-in context [:response :headers "Content-Type"]))
;;(update-in [:response] coerce-to (accepted-type context))))})


(defn respond-hello [request]
  (let [nm (get-in request [:query-params :name])
        resp (greeting-for nm)]
    (if resp
      (ok resp)
      (not-found))))

(def routes
  (route/expand-routes
   #{["/greet" :get [coerce-body content-neg-intc respond-hello] :route-name :greet]
     ["/echo"  :get echo]}))

(def service-map
  {::http/routes routes
   ::http/type :jetty
   ::http/port 8890})

(defn start []
  (http/start (http/create-server service-map)))

(defonce server (atom nil))

(defn start-dev []
  (reset! server
          (http/start (http/create-server
                       (assoc service-map
                              ::http/join? false)))))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))


;;old ok function, declares the content type
;;(defn ok [body]
  ;;{:body body
   ;;:headers {"Content-Type" "text/html"} ;;written in http
   ;;:status 200})

;;old coerce-body interceptor that is non-compact
;;(def coerce-body
  ;;{:name ::coerce-body
   ;;:leave
   ;;(fn [context]
     ;;(let [accepted (get-in context [:request :accept :field] "text/plain")
       ;;    response (get context :response)
         ;;  body (get response :body)
           ;;coerced-body (case accepted
             ;;             "text/html" body
               ;;           "text/plain" body
                 ;;         "application/edn" (pr-str body)
                   ;;       "application/json" (json/write-str body))
           ;;updated-response (assoc response
             ;;                      :headers {"Content-Type" accepted}
               ;;                    :body coerced-body)]
       ;;(assoc context :response updated-response)))})