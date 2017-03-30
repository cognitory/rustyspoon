(ns arborist.core-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [is deftest testing]])
            [clojure.zip :as z]
            [arborist.core :as a]))

(deftest searching-by-id
  (testing "can find nodes by annotated metadata"
    (let [data '(defn foo [x] ^{:id "foo"} (inc x))]
      (is (a/find-by-id data "foo"))
      (is (= '(inc x) (z/node (a/find-by-id data "foo")))))
    (let [data '(defn foo [x] (inc ^{:id "foo"} x))]
      (is (a/find-by-id data "foo"))
      (is (= 'x (z/node (a/find-by-id data "foo"))))
      (is (= (-> (a/find-by-id data "foo") (z/replace 'y) z/root)
             '(defn foo [x] (inc y)))))))

(deftest matching-test
  (testing "can see if selector matches at location"
    (let [zp (z/next (a/zipper '(defn foo [x] (inc y))))
          s (z/next (a/zipper '(defn foo (inc))))]
      (is (some? (a/matches-at? zp s))))
    (let [zp (z/next (a/zipper '(defn foo [x] inc x)))
          s (z/next (a/zipper '(defn foo (inc))))]
      (is (nil? (a/matches-at? zp s))))
    (let [zp (z/next (a/zipper '(defn foo [x] (inc x))))
          s (z/next (a/zipper '(defn foo (inc x))))]
      (is (a/matches-at? zp s)))))

(deftest selector-finding
  (testing "failing query"
    (is (nil? (a/zipper-at '[(ns foo) (defn app-view [x] x)]
                                 '(defn foobar))))
    (is (nil? (a/zipper-at '[(ns foo) (defn app-view [x] (inc x))]
                                 '(defn app-view (dec))))))
  (testing "simple search"
    (let [sel '(defn app-view)
          data '[(ns foo)
                 (defn app-view [stuff] stuff)]]
      (is (= (a/zipper-at data sel)
             (-> (a/zipper data) z/down z/right z/down z/next))))
    (let [sel '(defn app-view)
          data '[(ns foo)
                 (def foo {:x 1 :bar ["foo" "bar"]})
                 (defn app-view [stuff] stuff)]]
      (is (= (a/zipper-at data sel)
             (-> (a/zipper data) z/down z/right z/right z/down z/next))))
    (let [data '[(def foo {:x 1 :y 2})]
          sel '(def foo {:y 2})]
      (is (a/zipper-at data sel))))
  (testing "nested query"
    (let [sel '(defn app-view [:div [:ul (for [:li])]])
          data '[(ns foo)
                 (def foo {:x 1 :bar ["foo" "bar"]})
                 (defn app-view []
                   [:div
                    [:ul
                     (for [r restaurants]
                       [:li
                        [:div.name (r :name)]
                        [:div.address (r :address)]])]])]]
      (is (= (z/node (z/up (a/zipper-at data sel)))
             '[:li
               [:div.name (r :name)]
               [:div.address (r :address)]]))))
  (testing "multi-branch query"
    (let [data '[(foo [bar 1] [baz 2] [quux 3])]]
      (is (some? (a/zipper-at data '(foo [baz])))))))

(deftest modifying-tree-by-selector
  (testing "can append things"
    (let [sel '(defn app-view [:div [:ul (for (:li))]])
          data '[(ns foo)
                 (def foo {:x 1 :bar ["foo" "bar"]})
                 (defn app-view []
                   [:div
                    [:ul
                     (for [r restaurants]
                       [:li
                        [:div.name (r :name)]
                        [:div.address (r :address)]])]])]]
      (is (= (a/append-at data sel '[:div.rating (r :rating)])
             '[(ns foo)
               (def foo {:x 1 :bar ["foo" "bar"]})
               (defn app-view []
                 [:div
                  [:ul
                   (for [r restaurants]
                     [:li
                      [:div.name (r :name)]
                      [:div.address (r :address)]
                      [:div.rating (r :rating)]])]])])))
    (testing "with a multi-branch query"
      (let [data '[(foo [bar 1] [baz 2] [quux 3])]]
        (is (= (a/append-at data '(foo [baz]) '[zzz 9])
               '[(foo [bar 1] [baz 2 [zzz 9]] [quux 3])])))))
  (testing "can prepend things"
    (let [data '[(ns foo)
                 (def foo {:x 1 :bar ["foo" "bar"]})
                 (defn app-view []
                   [:div
                    [:ul
                     (for [r restaurants]
                       [:li
                        [:div.name (r :name)]
                        [:div.address (r :address)]])]])]
          sel '(defn app-view [:div [:ul (for [:li])]])]
      (is (= (a/prepend-at data sel '[:img {:src (r :img)}])
             '[(ns foo)
               (def foo {:x 1 :bar ["foo" "bar"]})
               (defn app-view []
                 [:div
                  [:ul
                   (for [r restaurants]
                     [:li
                      [:img {:src (r :img)}]
                      [:div.name (r :name)]
                      [:div.address (r :address)]])]])]))))
   (testing "can insert after"
    (let [sel '(defn app-view)
          data '[(ns foo) (defn app-view [x] (inc x))]]
      (is (= (a/insert-after data sel '(println "okay!"))
             '[(ns foo)
               (defn app-view [x] (inc x))
               (println "okay!")]))))
  (testing "can insert before"
    (let [sel '(defn app-view)
          data '[(ns foo)
                 (enable-console-print!)
                 (defn app-view [] [:div "hello"])]]
      (is (= (a/insert-before data sel '(def stuff [{:a 1 :b 2} {:a 7 :b 12}]))
             '[(ns foo)
               (enable-console-print!)
               (def stuff [{:a 1 :b 2} {:a 7 :b 12}])
               (defn app-view [] [:div "hello"])]))))
  (testing "can wrap things"
    (let [sel '(defn app-view (inc x))
          data '[(ns foo) (defn app-view [] (inc x))]]
      (is (= (a/wrap-with data sel identity)
             '[(ns foo) (defn app-view [] (inc x))]))
      (is (= (a/wrap-with data sel (fn [e] (list '* e 2)))
             '[(ns foo) (defn app-view [] (inc (* x 2)))]))))
  (testing "can replace things"
    (let [data '[(def foo {:x 1 :y 2})]
          sel '(def foo {:y 2})]
      (is (= (a/replace-with data sel 3)
             '[(def foo {:x 1 :y 3})])))))

(deftest modifying-tree-by-id
  (testing "can insert after"
    (let [data '[(ns foo) (defn ^{:id "view"} app-view [x] (inc x))]]
      (is (= (a/insert-after data "view" '(println "okay!"))
             '[(ns foo)
               (defn app-view [x] (inc x))
               (println "okay!")]))))
  (testing "can replace things"
    (let [data '(defn foo [x] (inc ^{:id "foo"} x))]
      (is (= (a/replace-with data "foo" 'y)
             '(defn foo [x] (inc y))))))
  (testing "can append things"
    (let [data '[(defn app-view [] [:div ^{:id "content"} [:h1 "Hello!"]])]]
      (is (= (a/append-at data "content" [:p "stuff"])
             '[(defn app-view [] [:div ^{:id "content"}
                                  [:h1 "Hello!"]
                                  [:p "stuff"]])]))))
  (testing "metadata stays in the tree"
    (let [data '[(defn app-view []
                   [:div ^{:id "content"}
                    [:h1 "Hello!"]])]]
      (is (= (-> data
                 (a/append-at "content" [:p "stuff"])
                 (a/append-at "content" [:p "more stuff"]))
             '[(defn app-view [] [:div ^{:id "content"}
                                  [:h1 "Hello!"]
                                  [:p "stuff"]
                                  [:p "more stuff"]])]))))
  (testing "can add metadata"
    (let [data '[(defn app-view []
                   [:div ^{:id "content"}
                    [:h1 "Hello!"]])]]
      (is (= (-> data
                 (a/replace-with "content"
                                 ^{:id "new-content"} [:h2 "things"])
                 (a/append-at "new-content" [:p "things"]))
             '[(defn app-view [] [:div ^{:id "content"}
                                  [:h2 "things"]
                                  [:p "things"]])])))
    (let [data '[(defn app-view []
                   ^{:id "content"}
                   [:div
                    [:h1 "Hello!"]])]]
      (is (= (-> data
                 (a/replace-with "content"
                                 [:section ^{:id "content"}
                                  [:p "stuff"]])
                 (a/append-at "content" [:p "things"]))
             '[(defn app-view [] [:section
                                  [:p "stuff"]
                                  [:p "things"]])])))))
