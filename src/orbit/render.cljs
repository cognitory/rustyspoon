(ns orbit.render
  (:require 
    [ajax.core :refer [GET]]
    [cljs.js :refer [empty-state eval-str js-eval]]
    [clojure.string :as string]
    [fipp.clojure :as fipp]
    [markdown.core :as md]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [reagent.ratom :include-macros true :refer-macros [reaction]]
    [secretary.core :as secretary :include-macros true :refer-macros [defroute]]
    [orbit.router :as router]
    [orbit.styles :refer [styles]]))

(enable-console-print!)

(defn tee [x]
  (println x) x)

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

(rf/register-sub
  :get-current-step
  (fn [app-state _]
    (reaction (:step @app-state))))

(rf/register-sub
  :get-steps
  (fn [app-state _]
    (reaction (get-in @app-state [:orbit :history]))))

(rf/register-sub
  :get-current-resources
  (fn [app-state _]
     (reaction (get-in @app-state [:orbit :history (:step @app-state) :resources]))))

(rf/register-sub
  :get-content
  (fn [app-state _]
    (reaction (get-in @app-state [:content]))))

(defroute step-path "/step/:name" [name]
  (rf/dispatch [:set-step-by-name! name]))

(defn- demo-view []
  [:div#app-wrapper
   [:div#app]])

(defn- file-view [file-name code]
  (let [highlight (fn [this]
                    (->> this
                         r/dom-node
                         (.-firstChild)
                         (.-nextSibling)
                         (.highlightBlock js/hljs)))]
    (r/create-class
      {:reagent-render
       (fn [file-name code]
         [:div.file
          [:div.name file-name]
          [:div.code.clojure
           (->> code
                (map #(with-out-str (fipp/pprint %1 {:width 50})))
                (string/join "\n"))]])
       :component-did-mount highlight
       :component-did-update highlight })))

(defn- resources-view []
  (let [resources (rf/subscribe [:get-current-resources]) ]
    (fn []
      [:div.resources
       (for [[file-name code] @resources]
         ^{:key file-name} [file-view file-name code])])))

(defn- steps-view []
  (let [steps (rf/subscribe [:get-steps])
        current-step (rf/subscribe [:get-current-step])]
    (fn []
      [:div.steps
       (doall
         (for [index (range (count @steps))]
           (let [step (get @steps index)
                 name (:step step)]
             [:div.step {:key index
                         :on-click (fn [_]
                                     (rf/dispatch [:set-step! index]))
                         :class (when (= index @current-step) "active")}
              name])))])))

(rf/register-sub
  :get-code-for-step
  (fn [app-state [_ step-id index-start index-end]]
    (let [index-end (if (js/isNaN index-end) index-start index-end)
          code (-> (get-in @app-state [:orbit :history])
                   (->> (filter (fn [s] (= step-id (s :step)))))
                   first
                   (get :step-actions)
                   (subvec index-start (inc index-end))
                   (->> (map #(with-out-str (fipp/pprint %1 {:width 50}))))
                   (->> (string/join "\n")))]
    (reaction code))))

(defn- md-add-snippets [text state]
  [(string/replace text
                   #"!!!([a-z \-]*?)/([0-9]*?)-([0-9]*?)!!!"
                   (fn [[_ step-id index-start index-end]]
                     (let [code (rf/subscribe [:get-code-for-step step-id
                                               (js/parseInt index-start)
                                               (js/parseInt index-end)])]
                       (str "<pre><code>" @code "</code></pre>"))))
   state])

(defn- md-add-go-to-step [text state]
  [(string/replace text
                   #"@@@([a-z \-]*?)@@@"
                   (fn [[_ step-id]]
                     (let [url (step-path {:name step-id})]
                       (str "<a href='" url "' class='run'>" "Run" "</a>"))))
   state])

(defn- tutorial-view []
  (let [content (rf/subscribe [:get-content])]
    (fn []
      [:div.tutorial
       {:dangerouslySetInnerHTML
        {:__html (md/md->html @content
                              :custom-transformers [md-add-snippets
                                                    md-add-go-to-step])}}])))

(defn orbit-view []
  [:div.orbit
   [:style {:type "text/css"
            :dangerouslySetInnerHTML {:__html styles}}]
   #_[steps-view]
   [tutorial-view]
   [resources-view]
   [demo-view]])

(defn render [orbit dom-target]
  (rf/dispatch-sync [:init! orbit])
  (r/render [orbit-view] dom-target)
  (GET (str "orbits/rustyspoon.md")
    {:handler (fn [raw-content]
                (rf/dispatch [:set-content! raw-content]))})
  #_(rf/dispatch [:set-step! 0]))

(defonce once
  (do
    (router/init!)))
