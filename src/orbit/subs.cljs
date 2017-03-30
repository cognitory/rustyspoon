(ns orbit.subs
  (:require
    [clojure.string :as string]
    [fipp.clojure :as fipp]
    [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-steps
  (fn [db _]
    (get-in db [:orbit :history])))

(reg-sub
  :get-current-resources
  (fn [db _]
     (get-in db [:orbit :history (:step db) :resources])))

(reg-sub
  :get-content
  (fn [db _]
    (get-in db [:content])))

(reg-sub
  :get-code-for-step
  (fn [db [_ step-id index-start index-end]]
    (let [index-end (if (js/isNaN index-end) index-start index-end)
          code (-> (get-in db [:orbit :history])
                   (->> (filter (fn [s] (= step-id (s :step)))))
                   first
                   (get :step-actions)
                   (subvec index-start (inc index-end))
                   (->> (map #(with-out-str (fipp/pprint %1 {:width 50}))))
                   (->> (string/join "\n")))]
      code)))

(reg-sub
  :get-current-step
  (fn [db _]
    (:step db)))
