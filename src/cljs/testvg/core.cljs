(ns testvg.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to testvg"]
   [:div [:a {:href "#/about"} "go to about page"]]
   [:div [:a {:href "#/test1"} "go to page: test1"]]
   [:div [:a {:href "#/test2"} "go to page: test2"]]
   [:div [:a {:href "#/test3"} "go to page: test3"]]])

(defn about-page []
  [:div [:h2 "About testvg"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn timer-component
  ([seconds-elapsed]
   (fn []
     (js/setTimeout #(swap! seconds-elapsed inc) 1000)
     [:div
      "Seconds Elapsed: " @seconds-elapsed]))
  ([seconds-elapsed started]
   (let [timeout-id (atom nil)]
     (fn []
       (if @started
         (->> (js/setTimeout #(swap! seconds-elapsed inc) 1000)
              (reset! timeout-id))
         (js/clearTimeout @timeout-id))
       [:div
        "Seconds Elapsed: " @seconds-elapsed]))))

(defn test1-page []
  [:div [:h2 "Test1"]
   [:div [:a {:href "#/"} "go to the home page"]]
   (let [seconds-elapsed (atom 0)]
     [timer-component seconds-elapsed])])

(defn test2-page []
  (let [seconds-elapsed (atom 0)]
    (fn []
      [:div [:h2 "Test2"]
       [:div [:a {:href "#/"} "go to the home page"]]
       [timer-component seconds-elapsed]
       [:div @seconds-elapsed]])))

(defn test3-page []
  (let [seconds-elapsed (atom 0)
        started (atom true)]
    (fn []
      [:div [:h2 "Test3"]
       [:div [:a {:href "#/"} "go to the home page"]]
       [timer-component seconds-elapsed started]
       [:div @seconds-elapsed]
       [:button {:type "button"
                 :on-click #(swap! started not)}
        (if @started "Stop" "Start")]])))

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/test1" []
  (session/put! :current-page #'test1-page))

(secretary/defroute "/test2" []
  (session/put! :current-page #'test2-page))

(secretary/defroute "/test3" []
  (session/put! :current-page #'test3-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
