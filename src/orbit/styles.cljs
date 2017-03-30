(ns orbit.styles
  (:require
    [garden.core :refer [css]]
    [garden.stylesheet :refer [at-import]]))

(def tutorial-styles
  [:>.tutorial
   {:min-width "20em"
    :flex-grow 2
    :height "100%"
    :overflow-x "scroll"

    :font-family "Alegreya"
    :line-height 1.5
    :padding "3em"
    :box-sizing "border-box"
    :white-space "pre-wrap"}

   [:h1
    {:font-size "1.5em"
     :margin-top 0}]

   [:h2
    {:font-size "1.25em"
     :margin-top "3em"}]

   [:h3
    {:font-size "1em"
     :margin-top "2em"}]

   [:a.run
    {:display "block"
     :background "#208000"
     :color "white"
     :padding "0.5em 0.75em"
     :margin "1em 0"
     :letter-spacing "0.2em"
     :font-weight "bold"
     :text-decoration "none"
     :border-radius "5px"
     :text-transform "uppercase"
     :font-family "Source Code Pro"}

    [:&::after
     {:content "\"▶\""
      :margin-left "0.5em"
      :font-size "0.75em"}]

    [:&:hover
     {:background "#519F50"}]]

   [:ol
    {:padding "0 3em"}

    [:li
     {:margin "1em 0"}]]

   [:p
    [:code
     {:background "#2B2852"
      :color "white"
      :padding "0.25em 0.4em"
      :border-radius "2px"}]]

   [:pre
    [:code
     {:font-family "Source Code Pro"
      :font-size "0.8em"
      :width "100%"
      :background "#2B2852"
      :color "white"
      :padding "1em"
      :border-radius "5px"
      :box-sizing "border-box"
      :display "block"
      :max-width "100%"
      :overflow "scroll"}]]])

(def resources-styles 
  [:>.resources
   {:min-width "30em"
    :flex-grow 3
    :height "100%"}

   [:.file
    {:height "100%"}

    [:.name
     {:display "none"}]

    [:.code
     {:font-family "Source Code Pro"
      :white-space "pre-wrap"
      :line-height "1.2"
      :font-size "0.8em"
      :padding "2em 2em"
      :height "100%"
      :overflow-x "scroll"
      :box-sizing "border-box"}]]])

(def app-wrapper-styles
  [:#app-wrapper
   {:min-width "20em"
    :height "100%"
    :flex-grow 1}

   [:#app
    {:background "white"
     :min-width "20em"
     :min-height "20em"
     :width "100%"
     :height "100%"
     :overflow-x "scroll"}]])

(def styles
  (css [(at-import "https://fonts.googleapis.com/css?family=Alegreya|Source+Code+Pro")

        [:.orbit
         {:display "flex"
          :position "absolute"
          :top 0
          :left 0
          :right 0
          :bottom 0
          :justify-content "space-between"}

         tutorial-styles
         resources-styles
         app-wrapper-styles]]))
