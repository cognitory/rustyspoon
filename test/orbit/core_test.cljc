(ns orbit.core-test
  (:require #?(:cljs [cljs.test :refer-macros [is deftest testing]]
               :clj [clojure.test :refer :all])
            [orbit.core :as o]))

#?(:cljs (enable-console-print!))

(deftest resources-and-add
  (testing "can add resources as a step"
    (let [orb (-> (o/init)
                  (o/step "add-files"
                          (o/resource "core.cljs")))]
      (is (= (keys orb) [:history]))
      (is (= 1 (count (orb :history))))
      (is (= {:step "add-files" :resources {"core.cljs" []} :step-actions []}
             (first (orb :history))))
      (testing "can add things to files"
        (let [orb (-> orb
                      (o/step "hello world"
                              (o/add "core.cljs"
                                '(ns foo.core
                                   (:require [foo.core :as f])))
                              (o/add "core.cljs"
                                '(enable-console-print!))
                              (o/add "core.cljs"
                                '(defn sq [x] (* x x)))))]
          (is (= 2 (count (orb :history))))
          (is (= (last (orb :history))
                 {:step "hello world"
                  :step-actions '[(ns foo.core
                                   (:require [foo.core :as f]))
                                  (enable-console-print!)
                                  (defn sq [x] (* x x))]
                  :resources
                  {"core.cljs" '[(ns foo.core
                                   (:require [foo.core :as f]))
                                 (enable-console-print!)
                                 (defn sq [x]
                                   (* x x))]}})))))))

(deftest adding-things-steps
  (let [orb (-> (o/init)
                (o/step "setup stuff"
                        (o/resource "core.cljs")
                        (o/add "core.cljs"
                          '(ns rustyspoon.core
                             (:require [reagent.core :as r])))
                        (o/add "core.cljs"
                          '(enable-console-print!))
                        (o/add "core.cljs"
                          '(defn app-view []
                             [:div "Hello world"])))
                (o/step "add array"
                        (o/before "core.cljs"
                                  '(defn app-view)
                                  '(def restaurants
                                     [{:name "You Eat"
                                       :address "1 Street St"
                                       :rating -5}
                                      {:name "Yes Yes Yes"
                                       :address "55 Fancy Ave"
                                       :rating 10.0}]))))]
    (testing "can insert things before"
      (is (= 2 (count (orb :history))))
      (is (= (get-in orb [:history 0 :resources "core.cljs"])
             '[(ns rustyspoon.core
                 (:require [reagent.core :as r]))
               (enable-console-print!)
               (defn app-view [] [:div "Hello world"])]))
      (is (= (get-in orb [:history 1 :resources "core.cljs"])
             '[(ns rustyspoon.core
                 (:require [reagent.core :as r]))
               (enable-console-print!)
               (def restaurants
                 [{:name "You Eat"
                   :address "1 Street St"
                   :rating -5}
                  {:name "Yes Yes Yes"
                   :address "55 Fancy Ave"
                   :rating 10.0}])
               (defn app-view [] [:div "Hello world"])])))

    (let [orb (-> orb
                  (o/step "add another function"
                          (o/after "core.cljs"
                            '(defn app-view)
                            '(println "Starting stuff!"))))]
      (testing "can insert things after"
        (is (= (get-in orb [:history 2 :resources "core.cljs"])
               '[(ns rustyspoon.core
                   (:require [reagent.core :as r]))
                 (enable-console-print!)
                 (def restaurants
                   [{:name "You Eat"
                     :address "1 Street St"
                     :rating -5}
                    {:name "Yes Yes Yes"
                     :address "55 Fancy Ave"
                     :rating 10.0}])
                 (defn app-view [] [:div "Hello world"])
                 (println "Starting stuff!")])))
      (let [orb (-> orb
                    (o/step "enhance view fn"
                            (o/append "core.cljs"
                              '(defn app-view [:div])
                              '[:p "This is some stuff"])
                            (o/append "core.cljs"
                              '(defn app-view [:div])
                              '[:p "This is some more stuff"])))]
        (testing "can append things"
          (is (= (get-in orb [:history 3 :resources "core.cljs"])
                 '[(ns rustyspoon.core
                     (:require [reagent.core :as r]))
                   (enable-console-print!)
                   (def restaurants
                     [{:name "You Eat"
                       :address "1 Street St"
                       :rating -5}
                      {:name "Yes Yes Yes"
                       :address "55 Fancy Ave"
                       :rating 10.0}])
                   (defn app-view []
                     [:div "Hello world"
                      [:p "This is some stuff"]
                      [:p "This is some more stuff"]])
                   (println "Starting stuff!")])))
        (testing "can prepend things"
          (let [orb (-> orb
                        (o/step "add more stuff"
                                (o/prepend "core.cljs"
                                           '(defn app-view [:div])
                                           '[:h1 "Things"])))]
            (is (= (get-in orb [:history 4 :resources "core.cljs"])
                   '[(ns rustyspoon.core
                       (:require [reagent.core :as r]))
                     (enable-console-print!)
                     (def restaurants
                       [{:name "You Eat"
                         :address "1 Street St"
                         :rating -5}
                        {:name "Yes Yes Yes"
                         :address "55 Fancy Ave"
                         :rating 10.0}])
                     (defn app-view []
                       [:div [:h1 "Things"]
                        "Hello world"
                        [:p "This is some stuff"]
                        [:p "This is some more stuff"]])
                     (println "Starting stuff!")]))))
        (testing "can wrap things"
          (let [orb (-> orb
                        (o/step "wrap text"
                                (o/wrap "core.cljs"
                                        '(defn app-view [:div "Hello world"])
                                        (fn [e] [:h1 e]))))]
            (is (= (get-in orb [:history 4 :resources "core.cljs"])
                   '[(ns rustyspoon.core
                       (:require [reagent.core :as r]))
                     (enable-console-print!)
                     (def restaurants
                       [{:name "You Eat"
                         :address "1 Street St"
                         :rating -5}
                        {:name "Yes Yes Yes"
                         :address "55 Fancy Ave"
                         :rating 10.0}])
                     (defn app-view []
                       [:div
                        [:h1 "Hello world"]
                        [:p "This is some stuff"]
                        [:p "This is some more stuff"]])
                     (println "Starting stuff!")]))))))))

(deftest steps-with-id
  (testing "can use ids to select & replace"
    (let [orb (-> (o/init)
                  (o/step "setup stuff"
                          (o/resource "core.cljs")
                          (o/add "core.cljs"
                            '(ns rustyspoon.core
                               (:require [reagent.core :as r])))
                          (o/add "core.cljs"
                            '(enable-console-print!))
                          (o/add "core.cljs"
                            ^{:id "app-view"}
                            '(defn app-view []
                               ^{:id "content"}
                               [:div "Hello world"])))
                  (o/step "add array"
                          (o/replace "core.cljs"
                                     "content"
                                     ^{:id "content"}
                                     [:li
                                      [:div
                                       [:h1 "Things"]]])))]
      (is (= (get-in (last (orb :history)) [:resources "core.cljs"])
             '[(ns rustyspoon.core
                 (:require [reagent.core :as r]))
               (enable-console-print!)
               (defn app-view []
                 [:li
                  [:div
                   [:h1 "Things"]]])]))
      (let [orb (-> orb
                    (o/step "add dict"
                            (o/before "core.cljs"
                                      "app-view"
                                      ^{:id "restaurant-data"}
                                      '(def restaurants
                                         [{:name "foo"
                                           :addres "bar"}
                                          {:name "baz"
                                           :address "quux"}]))))]))
    (let [orb (-> (o/init)
                  (o/step "setup stuff"
                          (o/resource "core.cljs")
                          (o/add "core.cljs"
                            '(ns rustyspoon.core
                               (:require [reagent.core :as r])))
                          (o/add "core.cljs"
                            '(enable-console-print!))
                          (o/add "core.cljs"
                            (quote
                              ^{:id "app-view"}
                              (defn app-view []
                                ^{:id "content"}
                                [:div "Hello world"]))))
                  (o/step "add array"
                          (o/replace "core.cljs"
                                     "app-view"
                                     (quote
                                       ^{:id "app-view"}
                                       (defn primary-view []
                                         (let [_ @(...)]
                                           ^{:id "content"}
                                           [:li
                                            [:div
                                             [:h1 "Things"]]]))))))]
      (is (= (get-in (last (orb :history)) [:resources "core.cljs"])
             '[(ns rustyspoon.core
                 (:require [reagent.core :as r]))
               (enable-console-print!)
               (defn primary-view []
                 (let [_ @(...)]
                   [:li
                    [:div
                     [:h1 "Things"]]]))]))
      (let [actions (:step-actions (last (orb :history)))]
        (is (= actions
               '[(defn primary-view []
                 (let [_ @(...)]
                   ^{:id "content"}
                   [:li
                    [:div
                     [:h1 "Things"]]]))]))))))
