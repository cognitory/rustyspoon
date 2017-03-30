(ns orbit.render
  (:require 
    [ajax.core :refer [GET]]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [orbit.events]
    [orbit.subs]
    [orbit.router :as router]
    [orbit.views :as views]))

(enable-console-print!)

(defn render [orbit dom-target]
  (rf/dispatch-sync [:init! orbit])
  (r/render [views/orbit-view] dom-target)
  (GET (str "orbits/rustyspoon.md")
    {:handler (fn [raw-content]
                (rf/dispatch [:set-content! raw-content]))}))

(defonce once
  (do
    (router/init!)))
