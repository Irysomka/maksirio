(ns maksirio.core
  (:require [maksirio.sprites :as sprites :refer [mario-sprite floor-sprite]]))

(enable-console-print!)

(defonce app-state (atom {:world []
                          :input-keys {:up nil :down nil :left nil :right nil :jump nil}
                          :player {:x 0 :y 84 :dy 0}}))

(defonce renderer (js/PIXI.autoDetectRenderer.
                   js/window.innerWidth
                   js/window.innerHeight,
                   #js {"antialias" true
                        "autoResize" true
                        "transparent" true
                        "resolution" 1
                        "clearBeforeRender" true}))

(defonce input-map {37 :left 39 :right 32 :jump})

(defn key-event [e v]
  (if-let [input (input-map (.-keyCode e))]
    (swap! app-state assoc-in [:input-keys input] v)))

(defn process-input []
  (let [{{:keys [left right jump]} :input-keys
         {:keys [dy y]} :player} @app-state]
    (if left (swap! app-state update-in [:player :x] #(- % 1.7)))
    (if right (swap! app-state update-in [:player :x] #(+ % 1.7)))
    (if (and jump (= 0 dy) (= 84 y)) (swap! app-state update-in [:player :dy] #(+ % 6))))

  (swap! app-state update-in [:player :y] #(- % (get-in @app-state [:player :dy])))

  (swap! app-state update-in [:player :dy] #(- % 0.4))

  (when (>= (get-in @app-state [:player :y]) 84)
    (swap! app-state assoc-in [:player :dy] 0)
    (swap! app-state assoc-in [:player :y] 84)))

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
   (generate-world)
   (.appendChild (.getElementById js/document "app") (.-view renderer))])

(defn draw-world []
  (let [stage (js/PIXI.Container.)
        {:keys [x y]} (:player @app-state)
        player (js/PIXI.Sprite. sprites/mario-sprite)]
    (set! (.-scale stage) (js/PIXI.Point. 2 2))

    (set! (.-x player) x)
    (set! (.-y player) y)
    (.addChild stage player)
    (doall (map
            #(let [{:keys [x y]} %
                   floor (js/PIXI.Sprite. sprites/floor-sprite)]
               (set! (.-x floor) x)
               (set! (.-y floor) y)
               (.addChild stage floor))
            (filter #(= :floor (:type %)) (:world @app-state))))
    (.render renderer stage)))

(defn main-loop []
  (.requestAnimationFrame js/window main-loop)
  (process-input)
  (draw-world))

(defonce init-main-loop
  (.requestAnimationFrame js/window main-loop))

(defn on-js-reload []
  )
