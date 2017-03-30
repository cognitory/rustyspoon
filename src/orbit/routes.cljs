(ns orbit.routes
  (:require
    [re-frame.core :as rf]
    [secretary.core :as secretary :include-macros true :refer-macros [defroute]]))

(defroute step-path "/step/:name" [name]
  (rf/dispatch [:set-step-by-name! name]))
