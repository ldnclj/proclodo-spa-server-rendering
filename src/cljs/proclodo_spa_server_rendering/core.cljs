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
;; Routes
(def routes ["/" {""      :home-page
                  "about" :about-page}])

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (pushy/start! (pushy/pushy #(session/put! :current-page
                                            (case (:handler %)
                                              :home-page #'home-page
                                              :about-page #'about-page))
                             (partial bidi/match-route routes)))
  (mount-root))
