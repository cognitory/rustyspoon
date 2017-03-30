(ns orbit.events
  (:require
    [clojure.string :as string]
    [cljs.js :refer [empty-state eval-str js-eval]]
    [re-frame.core :as rf]))

(defn- eval-code [code]
  (eval-str (empty-state)
            (string/join "\n" code)
            'dummy-symbol
            {:ns 'cljs.user
             :static-fns true
             :def-emits-var false
             :eval js-eval
             ; NOTE: load does nothing; libs must be reqd by this ns
             :load (fn [name cb] (cb {:lang :clj :source "."}))
             :context :statement}
            (fn [{:keys [error value] :as x}]
              (if error
                (do
                  (def *er x)
                  (js/console.log (str error)))))))

(defn- eval-current-code [app-state step]
  (eval-code (get-in app-state [:orbit :history step :resources "core.cljs"])))

(defn set-step! [app-state [_ step]]
  (eval-current-code app-state step)
  (assoc app-state :step step))

(defn init! [app-state [_ orbit]]
  (assoc app-state :orbit orbit))

(rf/register-handler :init! init!)
(rf/register-handler :set-step! set-step!)
(rf/register-handler :set-content!
                     (fn [app-state [_ content]]
                          (assoc app-state :content content)))
(rf/register-handler
  :set-step-by-name!
  (fn [app-state [_ name]]
       (let [id (->> (get-in app-state [:orbit :history])
                     (keep-indexed (fn [idx s] (when (= name (s :step)) idx)))
                     first)]
         (set-step! app-state [nil id]))))


