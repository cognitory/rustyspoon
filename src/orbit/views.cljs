(ns orbit.views
  (:require
    [clojure.string :as string]
    [fipp.clojure :as fipp]
    [markdown.core :as md]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [orbit.routes :as routes]
    [orbit.styles :refer [styles]]))

(defn md-add-snippets [text state]
  [(string/replace text
                   #"!!!([a-z \-]*?)/([0-9]*?)-([0-9]*?)!!!"
                   (fn [[_ step-id index-start index-end]]
                     (let [code (rf/subscribe [:get-code-for-step step-id
                                               (js/parseInt index-start)
                                               (js/parseInt index-end)])]
                       (str "<pre><code>" @code "</code></pre>"))))
   state])

(defn md-add-go-to-step [text state]
  [(string/replace text
                   #"@@@([a-z \-]*?)@@@"
                   (fn [[_ step-id]]
                     (let [url (routes/step-path {:name step-id})]
                       (str "<a href='" url "' class='run'>" "Run" "</a>"))))
   state])

(defn tutorial-view []
  (let [content (rf/subscribe [:get-content])]
    (fn []
      [:div.tutorial
       {:dangerouslySetInnerHTML
        {:__html (md/md->html @content
                              :custom-transformers [md-add-snippets
                                                    md-add-go-to-step])}}])))

(defn file-view [file-name code]
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

(defn resources-view []
  (let [resources (rf/subscribe [:get-current-resources]) ]
    (fn []
      [:div.resources
       (for [[file-name code] @resources]
         ^{:key file-name} [file-view file-name code])])))

(defn demo-view []
  [:div#app-wrapper
   [:div#app]])

(defn orbit-view []
  [:div.orbit
   [:style {:type "text/css"
            :dangerouslySetInnerHTML {:__html styles}}]
   [tutorial-view]
   [resources-view]
   [demo-view]])
