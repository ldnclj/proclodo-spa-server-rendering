(ns proclodo-spa-server-rendering.prod
  (:require [proclodo-spa-server-rendering.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
