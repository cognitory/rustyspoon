(ns orbit.core
  (:refer-clojure :exclude [replace])
  #?(:cljs (:require-macros [orbit.core :refer [deftx]]))
  (:require [arborist.core :as a]))

(defn init []
  {:history []})

(comment
  "a state is:"
  {:resources {"resource-name" '()}
   :step-actions []
   :step "step name"})

(defn- advance
  "helper function to modify last state by func and store in history"
  [orbit func]
  (update orbit :history conj (func (last (orbit :history)))))

(defn step
  "applies collection of txs "
  [orbit step-name & txs]
  (advance orbit
    (fn [state]
      (reduce (fn [st tx] (tx st))
              (-> state
                  (assoc :step step-name)
                  (assoc :step-actions []))
              txs))))

; transactions

(defn resource
  "creates a new resource"
  [resource-name]
  (fn [state]
    (assoc-in state [:resources resource-name] [])))

; each tx returns a function that modifies state

(defn- tx
  "helper fn for creating txs; returns a function that modifies state"
  [r func]
  (fn [state]
    (update-in state [:resources r] func)))

(defn- action-change
  "helper fn for keeping track of what an action is adding"
  [form]
  (fn [state] (update-in state [:step-actions] conj form)))

#?(:clj
(defmacro deftx [tx-name args tx-fn]
  (let [form (last args)]
    `(defn ~tx-name [r# ~@args]
       (comp
         (action-change ~form)
         (tx r# ~tx-fn)))))
)

(deftx add [form]
  (fn [forms]
    (conj forms form)))

(deftx before [pattern form]
  (fn [forms]
    (a/insert-before forms pattern form)))

(deftx after [pattern form]
  (fn [forms]
    (a/insert-after forms pattern form)))

(deftx append [pattern form]
  (fn [forms]
    (a/append-at forms pattern form)))

(deftx prepend [pattern form]
  (fn [forms]
    (a/prepend-at forms pattern form)))

(deftx wrap [pattern wrap-form]
  (fn [forms]
    (a/wrap-with forms pattern wrap-form)))

(deftx replace [pattern form]
  (fn [forms]
    (a/replace-with forms pattern form)))

(comment

(defn remove [r pattern]
  (tx r (fn [forms]
          forms ;TODO
          )))

(defn assoc [r pattern k v]
   (tx r (fn [forms]
           forms ;TODO
           )))

 (defn dissoc [r pattern k]
   (tx r (fn [forms]
           forms ;TODO
           )))
 )

