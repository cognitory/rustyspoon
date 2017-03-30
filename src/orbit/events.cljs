(ns orbit.events
  (:require
    [clojure.string :as string]
    [cljs.js :refer [empty-state eval-str js-eval]]
    [re-frame.core :refer [reg-event-fx]]))

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


(reg-event-fx
  :-eval-current-code
  (fn [{db :db} [_ step]]
    (eval-code (get-in db [:orbit :history step :resources "core.cljs"]))
    {}))

(reg-event-fx 
  :init! 
  (fn [{db :db} [_ orbit]]
    {:db (assoc db :orbit orbit)}))

(reg-event-fx 
  :set-step! 
  (fn [{db :db} [_ step]]
    {:dispatch [:-eval-current-code step]
     :db (assoc db :step step)}))

(reg-event-fx 
  :set-content!
  (fn [{db :db} [_ content]]
    {:db (assoc db :content content)}))

(reg-event-fx
  :set-step-by-name!
  (fn [{db :db} [_ name]]
    (let [id (->> (get-in db [:orbit :history])
                  (keep-indexed (fn [idx s] (when (= name (s :step)) idx)))
                  first)]
      {:dispatch [:set-step! id]})))


