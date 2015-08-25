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
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes and wiring
(def routes ["/" {""      :home-page
                  "about" :about-page}])

(def parse-path (partial bidi/match-route routes))

(defn set-current-page [parsed-path]
  (session/put! :current-page
                (case (:handler parsed-path)
                  :home-page #'home-page
                  :about-page #'about-page)))

(defn render-page [path]
  (set-current-page (parse-path path))
  (reagent/render-to-string [current-page]))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init []
  (pushy/start! (pushy/pushy set-current-page parse-path))
  (mount-root))
