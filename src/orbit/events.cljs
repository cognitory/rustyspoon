(ns orbit.events
  (:require
    [re-frame.core :refer [reg-event-fx reg-fx]]
    [orbit.eval :refer [eval-code]]))

(reg-fx :eval-code eval-code)

(reg-event-fx
  :-eval-current-code
  (fn [{db :db} [_ step]]
    {:eval-code (get-in db [:orbit :history step :resources "core.cljs"])}))

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


