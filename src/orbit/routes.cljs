(ns orbit.routes
  (:require
    [re-frame.core :refer [dispatch]]
    [secretary.core :refer-macros [defroute]]))

(defroute step-path "/step/:name" [name]
  (dispatch [:set-step-by-name! name]))
