(ns proclodo-spa-server-rendering.handler
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]])
  (:import [javax.script
            Invocable
            ScriptEngineManager]))

(defn render-app [path]
  (let [js (doto (.getEngineByName (ScriptEngineManager.) "nashorn")
             (.eval "var global = this")                    ; React requires either "window" or "global" to be defined.
             (.eval (-> "public/js/app.js"                  ; TODO: load the console polyfill, so that calling console.log is safe.
                        io/resource
                        io/reader)))
        page-content (.invokeMethod
                       ^Invocable js
                       (.eval js "proclodo_spa_server_rendering.core")
                       "render_page"
                       (object-array [path]))]
    (html
      [:html
       [:head
        [:meta {:charset "utf-8"}]
        [:meta {:name    "viewport"
                :content "width=device-width, initial-scale=1"}]
        (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
       [:body
        [:div#app page-content]
        (include-js "js/app.js")
        [:script {:type "text/javascript"} "proclodo_spa_server_rendering.core.init()"]]])))

(defroutes routes
           (GET "*" request
             (render-app (str (:uri request) ; Build the path the same way ring.util.request/request-url does it: https://github.com/ring-clojure/ring/blob/1.4.0/ring-core/src/ring/util/request.clj#L5
                              (if-let [query (:query-string request)]
                                (str "?" query)))))
           (resources "/")
           (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
