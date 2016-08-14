(ns maksirio.core
  (:require [reagent.core :as r :refer [atom]]
            [cljsjs.react-pixi]))

(enable-console-print!)

(defonce app-state (atom {:x 0 :y 84 :up nil :down nil :left nil :right nil}))

(def stage (r/adapt-react-class js/ReactPIXI.Stage))
(def text (r/adapt-react-class js/ReactPIXI.Text))
(def sprite (r/adapt-react-class js/ReactPIXI.Sprite))

(def mario-spritesheet
  (.fromImage js/PIXI.Texture "images/mario_sprites.gif" ))

(def mario-sprite
  (js/PIXI.Texture. mario-spritesheet (js/PIXI.Rectangle. 23 507 13 16)))

(def floor-sprite
  (js/PIXI.Texture. mario-spritesheet (js/PIXI.Rectangle. 373 124 16 16)))

(def input-map {37 :left 39 :right})

(defn key-event [e v]
  (if-let [input (input-map (.-keyCode e))]
    (swap! app-state assoc input v)))

(defn input-loop []
  (.requestAnimationFrame js/window input-loop)
  (let [{:keys [left right]} @app-state]
    (if left (swap! app-state update :x dec))
    (if right (swap! app-state update :x inc))))

(defonce listeners
  [(.addEventListener js/window "keydown" #(key-event % true))
   (.addEventListener js/window "keyup" #(key-event % false))
   (input-loop)])

(defn root []
  (let [{:keys [x y]} @app-state]
    [stage {:width (- (.-innerWidth js/window) 5)
           :height (- (.-innerHeight js/window) 5)
           :backgroundcolor 0xffffff
           :scale (js/PIXI.Point. 2 2)}
    [sprite {:x x :y y :texture mario-sprite}]
    (for [x (range 100)]
      [sprite {:x (* x 16)
               :y 100
               :texture floor-sprite
               :key (str "floor-" x)}])]))

(r/render-component [root]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
