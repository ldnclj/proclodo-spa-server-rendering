(ns proclodo-spa-server-rendering.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]))

;; -------------------------
;; Views
(defn home-page []
  [:div [:h2 "Welcome to proclodo-spa-server-rendering"]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About proclodo-spa-server-rendering"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [(session/get :current-page)])

;; -------------------------
;; Routes and wiring
(def routes ["/" {""      :home-page
                  "about" :about-page}])

(defn parse-path [path]
  (case (:handler (bidi/match-route routes path))
    :home-page #'home-page
    :about-page #'about-page
    (throw (js/Error. (str "Path not recognized: " (pr-str path))))))

(defn set-current-page [parsed-path]
  (session/put! :current-page parsed-path))

(defn ^:export render-page [path]
  (reagent/render-to-string [(parse-path path)]))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn ^:export init []
  (pushy/start! (pushy/pushy set-current-page parse-path))
  (mount-root))
