(ns proclodo-spa-server-rendering.handler
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [aleph.flow :as pool])
  (:import [javax.script
            Invocable
            ScriptEngineManager]
           [io.aleph.dirigiste Pools]))

(defn- create-js-engine []
  (doto (.getEngineByName (ScriptEngineManager.) "nashorn")
    (.eval "var global = this")                             ; React requires either "window" or "global" to be defined.
    (.eval (-> "public/js/server-side.js"                   ; TODO: load the console polyfill, so that calling console.log is safe.
               io/resource
               io/reader))))

(def js-engine-pool (pool/instrumented-pool {:generate   (fn [_]
                                                           (println "Creating new js-engine")
                                                           (let [js-engine (create-js-engine)]
                                                             (println "js-engine created:" js-engine)
                                                             js-engine))
                                             :controller (Pools/fixedController 10000 10000)}))

(defn- render-app [path]
  (println "Rendering:" path)
  (letfn [(render-page [path] (let [js-engine @(pool/acquire js-engine-pool "")]
                                (println "Using enigne:" js-engine)
                                (try
                                  (.invokeMethod
                                   ^Invocable js-engine
                                   (.eval js-engine "proclodo_spa_server_rendering.core")
                                   "render_page"
                                   (object-array [path]))
                                  (finally (pool/release js-engine-pool "" js-engine)))))]
    (html
      [:html
       [:head
        [:meta {:charset "utf-8"}]
        [:meta {:name    "viewport"
                :content "width=device-width, initial-scale=1"}]
        (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
       [:body
        [:div#app (render-page path)]
        (include-js "js/app.js")
        [:script {:type "text/javascript"} "proclodo_spa_server_rendering.core.init()"]]])))

(defn- path [request]
  (str (:uri request)                                       ; Build the path the same way ring.util.request/request-url does it: https://github.com/ring-clojure/ring/blob/1.4.0/ring-core/src/ring/util/request.clj#L5
       (if-let [query (:query-string request)]
         (str "?" query))))

(defroutes routes
           (GET "*" request
             (render-app (path request)))
           (resources "/")
           (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
