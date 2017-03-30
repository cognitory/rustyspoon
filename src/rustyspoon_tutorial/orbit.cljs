(ns rustyspoon-tutorial.orbit
  (:require 
    [orbit.core :as o]
    [orbit.render :refer [render]]))

(def orbit
  (-> (o/init)

      (o/step "init"
              (o/resource "core.cljs"))

      (o/step "hello world"
              (o/add "core.cljs"
                '(ns rustyspoon.core
                   (:require 
                     [clojure.string :as string]
                     [reagent.core :as r]
                     [garden.core :as garden])))

              (o/add "core.cljs"
                '(enable-console-print!))

              (o/add "core.cljs"
                '(println "Hello Console!"))

              (o/add "core.cljs"
                (quote
                  ^{:id "app-view"}
                  (defn app-view []
                    [:div "Hello World!"])))

              (o/add "core.cljs"
                '(r/render [app-view] (js/document.getElementById "app"))))

      (o/step "define restaurants array"
              (o/before "core.cljs"
                        '(defn app-view)
                        (quote
                          ^{:id "restaurants-data"}
                          (def restaurants
                            [{:name "Byblos"
                              :address "11 Duncan Street"
                              :image "kgXfBW9csGml_ZicwCB5Xg"
                              :rating 4.5
                              :price-range 3 }
                             {:name "George"
                              :address "111 Queen St. E"
                              :image "gH783lm_UYR8b78s3Ul5Rg"
                              :rating 4.4
                              :price-range 4 }
                             {:name "Kaiju"
                              :address "384 Yonge St."
                              :image "WQvsAGnWJcjUQQH3DMw8gA"
                              :rating 4.3
                              :price-range 1 }
                             {:name "Richmond Station"
                              :address "1 Richmond St West"
                              :image "AGtyni4gZtoWSRz_U0Axwg"
                              :rating 4.2
                              :price-range 3 }
                             {:name "Banh Mi Boys"
                              :address "392 Queen St. West"
                              :image "S1JS93tjQLqSwXMeWz0z7g"
                              :rating 4.0
                              :price-range 1 }
                             {:name "Canoe"
                              :address "66 Wellington St."
                              :image "g0lZAilNKqlfQTNLUtWp3Q"
                              :rating 3.9
                              :price-range 4 }]))))


      (o/step "update app-view to show restaurants"
              (o/replace "core.cljs"
                         "app-view"
                         (quote
                           ^{:id "app-view"}
                           (defn app-view []
                             [:div.app
                              (for [r restaurants]
                                ^{:id "restaurant-div"}
                                [:div.restaurant
                                 [:div.name (r :name)]
                                 [:div.address (r :address)]])]))))

      (o/step "add image function"
              (o/before "core.cljs"
                        '(def restaurants)
                        '(defn id->image [id]
                           (str "https://s3-media2.fl.yelpcdn.com/bphoto/" id "/ls.jpg"))))

      (o/step "display image"
              (o/before "core.cljs"
                        '(defn app-view [:div.app (for (:div.restaurant (:div.name)))])
                        '[:img {:src (id->image (r :image))}]))

      (o/step "show other restaurant info"

              (o/append "core.cljs"
                '(defn app-view [:div.app (for (:div.restaurant))])
                '[:div.rating (r :rating)])

              (o/append "core.cljs"
                '(defn app-view [:div.app (for (:div.restaurant))])
                '[:div.price-range (repeat (r :price-range) "$")]))

      (o/step "add styles"
              (o/before "core.cljs"
                        '(defn id->image)
                        '(def styles
                           (garden/css
                             (let [h "5em"]
                               [:.app
                                [:.restaurant
                                 {:height h
                                  :margin "1em"}
                                 [:img
                                  {:width h
                                   :height h
                                   :float "left"
                                   :margin-right "0.5em"}]
                                 [:.name
                                  {:font-weight "bold"}]
                                 [:.price-range
                                  {:color "green"}]]]))))

              (o/prepend "core.cljs"
                         '(defn app-view [:div.app])
                         '[:style styles]))

(o/step "factor out a restaurant-view"
        (o/before "core.cljs"
                  '(defn app-view)
                  '(defn restaurant-view [r]
                     [:div.restaurant
                      [:img {:src (id->image (r :image))}]
                      [:div.name (r :name)]
                      [:div.address (r :address)]
                      [:div.rating (r :rating)]
                      [:div.price-range
                       (repeat (r :price-range) "$")]]))

        (o/replace "core.cljs"
                   "restaurant-div"
                   (quote
                     [restaurant-view r])))

(o/step "add a header-view"
        (o/before "core.cljs"
                  '(defn restaurant-view)
                  '(defn header-view []
                     [:div.header
                      ^{:id "input.search"}
                      [:input.search {:placeholder "Search"}]
                      ^{:id "div.filter"}
                      [:div.filter
                       [:button "$"]
                       [:button "$$"]
                       [:button "$$$"]
                       [:button "$$$$"]]
                      ^{:id "div.sort"}
                      [:div.sort
                       [:button.name "Name"]
                       [:button.rating "Rating"]]]))

        (o/before "core.cljs"
                  '(defn app-view (:div.app (for)))
                  '[header-view])

        (o/prepend "core.cljs"
                   '(def styles (garden/css (let (:.app))))
                   '[:.header
                     {:background "#CD5C5C"
                      :margin-bottom "1em"
                      :padding "1em"}
                     [:.search
                      {:width "100%"
                       :border-radius "5px"
                       :border "none"
                       :padding "0.5em"
                       :margin-bottom "1em"
                       :box-sizing "border-box"}]]))

(o/step "sorting our list"
        (o/replace "core.cljs"
                   "app-view"
                   (quote
                     ^{:id "app-view"}
                     (defn app-view []
                       [:div.app
                        [:style styles]
                        [header-view]
                        (for [r (sort-by :rating restaurants)]
                          [restaurant-view r])]))))

(o/step "reverse the sort"
        (o/replace "core.cljs"
                   "app-view"
                   (quote
                     ^{:id "app-view"}
                     (defn app-view []
                       [:div.app
                        [:style styles]
                        [header-view]
                        (for [r (reverse (sort-by :rating restaurants))]
                          [restaurant-view r])]))))

(o/step "implementing sort toggle"
        (o/before "core.cljs"
                  '(def styles)
                  (quote
                    ^{:id "app-state"}
                    (def app-state (r/atom {:sort :rating}))))

        (o/replace "core.cljs"
                   "app-view"
                   '(defn app-view []
                      [:div.app
                       [:style styles]
                       [header-view]
                       (for [r ^{:id "restaurants"} (reverse (sort-by (@app-state :sort) restaurants))]
                         [restaurant-view r])]))

        (o/before "core.cljs"
                  '(def styles)
                  '(defn set-sort! [sort]
                     (swap! app-state (fn [state] (assoc state :sort sort)))))

        (o/replace "core.cljs"
                   "div.sort"
                   (quote
                     ^{:id "div.sort"}
                     [:div.sort
                      [:button {:on-click (fn [_] (set-sort! :name))} "Name"]
                      [:button {:on-click (fn [_] (set-sort! :rating))} "Rating"]])))

(o/step "styling sort buttons"
        (o/replace "core.cljs"
                   "div.sort"
                   '[:div.sort
                     [:button {:class (when (= :name (@app-state :sort)) "active")
                               :on-click (fn [_] (set-sort! :name))}
                      "Name"]
                     [:button {:class (when (= :rating (@app-state :sort)) "active")
                               :on-click (fn [_] (set-sort! :rating))}
                      "Rating"]])

        (o/append "core.cljs"
          '(def styles (garden/css (let (:.app (:.header)))))
          (quote
            ^{:id "button"}
            [:button
             {:background "grey"}
             [:&.active
              {:background "red"}]])))

(o/step "styling buttons better"
        (o/append "core.cljs"
          '(def styles (garden/css (let (:.app (:.header)))))
          '[:.filter
            {:display "inline-block"
             :margin-right "1em"}])

        (o/append "core.cljs"
          '(def styles (garden/css (let (:.app (:.header)))))
          '[:.sort
            {:display "inline-block"}])

        (o/replace "core.cljs"
                   "button"
                   (quote
                     [:button
                      {:border-radius "5px"
                       :border "none"
                       :margin-right "0.5em"
                       :background "#D68686"
                       :outline "none"
                       :cursor "pointer"}
                      [:&.active
                       {:background "#FFF"}]
                      ])))

(o/step "price range filtering"

        (o/replace "core.cljs"
                   "app-state"
                   (quote
                     ^{:id "app-state"}
                     (def app-state (r/atom {:sort :rating
                                             :price-ranges #{1 2 3 4}}))))

        (o/replace "core.cljs"
                   "restaurants"
                   (quote
                     ^{:id "restaurants"}
                     (->> restaurants
                          (sort-by (@app-state :sort))
                          reverse
                          (filter (fn [r] (contains? (@app-state :price-ranges) (r :price-range)))))))

        (o/after "core.cljs"
          '(defn set-sort!)
          '(defn toggle-price-range! [price-range]
             (if (contains? (@app-state :price-ranges) price-range)
               (swap! app-state (fn [state] (assoc state :price-ranges (disj (state :price-ranges) price-range))))
               (swap! app-state (fn [state] (assoc state :price-ranges (conj (state :price-ranges) price-range)))))))

        (o/replace "core.cljs"
                   "div.filter"
                   (quote
                     ^{:id "div.filter"}
                     [:div.filter
                      [:button {:on-click (fn [_] (toggle-price-range! 1))} "$"]
                      [:button {:on-click (fn [_] (toggle-price-range! 2))} "$$"]
                      [:button {:on-click (fn [_] (toggle-price-range! 3))} "$$$"]
                      [:button {:on-click (fn [_] (toggle-price-range! 4))} "$$$$"]]))

        (o/replace "core.cljs"
                   "div.filter"
                   (quote
                     ^{:id "div.filter"}
                     [:div.filter
                      [:button {:class (when (contains? (@app-state :price-ranges) 1) "active")
                                :on-click (fn [_] (toggle-price-range! 1))} "$"]
                      [:button {:class (when (contains? (@app-state :price-ranges) 2) "active")
                                :on-click (fn [_] (toggle-price-range! 2))} "$$"]
                      [:button {:class (when (contains? (@app-state :price-ranges) 3) "active")
                                :on-click (fn [_] (toggle-price-range! 3))} "$$$"]
                      [:button {:class (when (contains? (@app-state :price-ranges) 4) "active")
                                :on-click (fn [_] (toggle-price-range! 4))} "$$$$"]])))

(o/step "refactor price range buttons"
        (o/replace "core.cljs"
                   "div.filter"
                   (quote
                     ^{:id "div.filter"}
                     [:div.filter
                      (for [p [1 2 3 4]]
                        [:button {:class (when (contains? (@app-state :price-ranges) p) "active")
                                  :on-click (fn [_] (toggle-price-range! p))}
                         (repeat p "$")]) ])))

(o/step "search"
        (o/replace "core.cljs"
                   "app-state"
                   '(def app-state (r/atom {:sort :rating
                                            :price-ranges #{1 2 3 4}
                                            :query ""})))
        (o/after "core.cljs"
          '(defn toggle-price-range!)
          '(defn set-query! [query]
             (swap! app-state (fn [state] (assoc state :query query)))))


        (o/replace "core.cljs"
                   "input.search"
                   '[:input.search {:on-change (fn [e]
                                                (set-query! (.. e -target -value)))
                                   :placeholder "Search"}])

        (o/replace "core.cljs"
                   "restaurants"
                   '(->> restaurants
                         (sort-by (@app-state :sort))
                         reverse
                         (filter (fn [r] (contains? (@app-state :price-ranges) (r :price-range))))
                         (filter (fn [r] (clojure.string/includes?
                                           (string/lower-case (r :name))
                                           (string/lower-case (@app-state :query))))))))

      ;... more steps
      ))

(render orbit (.. js/document (getElementById "rustyspoon-tutorial")))

