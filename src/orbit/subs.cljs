(ns orbit.subs
  (:require
    [clojure.string :as string]
    [fipp.clojure :as fipp]
    [reagent.ratom :include-macros true :refer-macros [reaction]]
    [re-frame.core :as rf]))

(rf/register-sub
  :get-steps
  (fn [app-state _]
    (reaction (get-in @app-state [:orbit :history]))))

(rf/register-sub
  :get-current-resources
  (fn [app-state _]
     (reaction (get-in @app-state [:orbit :history (:step @app-state) :resources]))))

(rf/register-sub
  :get-content
  (fn [app-state _]
    (reaction (get-in @app-state [:content]))))

(rf/register-sub
  :get-code-for-step
  (fn [app-state [_ step-id index-start index-end]]
    (let [index-end (if (js/isNaN index-end) index-start index-end)
          code (-> (get-in @app-state [:orbit :history])
                   (->> (filter (fn [s] (= step-id (s :step)))))
                   first
                   (get :step-actions)
                   (subvec index-start (inc index-end))
                   (->> (map #(with-out-str (fipp/pprint %1 {:width 50}))))
                   (->> (string/join "\n")))]
    (reaction code))))

(rf/register-sub
  :get-current-step
  (fn [app-state _]
    (reaction (:step @app-state))))
