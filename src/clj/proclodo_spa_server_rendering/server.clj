(ns proclodo-spa-server-rendering.server
  (:require [proclodo-spa-server-rendering.handler :refer [app]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (let [config {:port        (Integer/parseInt (or (env :port) "3000"))
                :join?       false
                :min-threads (when (env :min-threads) (Integer/parseInt (env :min-threads)))
                :max-threads (when (env :max-threads) (Integer/parseInt (env :max-threads)))}
        config (reduce-kv #(if (not (nil? %3)) (assoc %1 %2 %3) %1) {} config)]
    (println "Running with config:" config)
    (run-jetty app config)))
