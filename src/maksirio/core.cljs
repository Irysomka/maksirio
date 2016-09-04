(ns maksirio.core
  (:require [reagent.core :as r :refer [atom]]
            [cljsjs.react-pixi]))

(enable-console-print!)

(defonce app-state (atom {:world []
                          :input-keys {:up nil :down nil :left nil :right nil :jump nil}
                          :player {:x 0 :y 84 :dy 0}}))

(def stage (r/adapt-react-class js/ReactPIXI.Stage))
(def text (r/adapt-react-class js/ReactPIXI.Text))
(def sprite (r/adapt-react-class js/ReactPIXI.Sprite))

(def mario-spritesheet
  (.fromImage js/PIXI.Texture "images/mario_sprites.gif" ))

(def mario-sprite
  (js/PIXI.Texture. mario-spritesheet (js/PIXI.Rectangle. 23 507 13 16)))

(def floor-sprite
  (js/PIXI.Texture. mario-spritesheet (js/PIXI.Rectangle. 373 124 16 16)))

(def input-map {37 :left 39 :right 32 :jump})

(defn key-event [e v]
  (if-let [input (input-map (.-keyCode e))]
    (swap! app-state assoc-in [:input-keys input] v)))

(defn input-loop []
  (.requestAnimationFrame js/window input-loop)
  (let [{{:keys [left right jump]} :input-keys
         {:keys [dy y]} :player} @app-state]
    (if left (swap! app-state update-in [:player :x] #(- % 1.7)))
    (if right (swap! app-state update-in [:player :x] #(+ % 1.7)))
    (if (and jump (= 0 dy) (= 84 y)) (swap! app-state update-in [:player :dy] #(+ % 6))))

  (swap! app-state update-in [:player :y] #(- % (get-in @app-state [:player :dy])))

  (swap! app-state update-in [:player :dy] #(- % 0.4))

  (when (>= (get-in @app-state [:player :y]) 84)
    (swap! app-state assoc-in [:player :dy] 0)
    (swap! app-state assoc-in [:player :y] 84)
  ))

(defn generate-world []
  (doall
   (for [x (range 100)]
     (swap! app-state
            update
            :world
            #(conj % {:type :floor :x (* x 16) :y 100})))))

(defonce listeners
  [(.addEventListener js/window "keydown" #(key-event % true))
   (.addEventListener js/window "keyup" #(key-event % false))
   (input-loop)
   (generate-world)])

(defn draw-floor []
  (map
   #(let [x (:x %) y (:y %)]
      [sprite {:x x
               :y y
               :texture floor-sprite
               :key (str "floor-" x)}])
   (filter #(= :floor (:type %)) (:world @app-state))))

(defn root []
  (let [{:keys [x y]} (:player @app-state)]
    [stage {:width (- (.-innerWidth js/window) 5)
           :height (- (.-innerHeight js/window) 5)
           :backgroundcolor 0xffffff
           :scale (js/PIXI.Point. 2 2)}
    (draw-floor)
    [sprite {:x x :y y :texture mario-sprite}]]))

(r/render-component [root]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
