(ns maksirio.core
  (:require [maksirio.draw :as draw :refer [world]]))

(enable-console-print!)

(defonce app-state (atom {:world [{:type :floor :x 100 :y 150}]
                          :input-keys {:up nil :down nil :left nil :right nil :jump nil}
                          :player {:x 0 :y 100 :dy 0 :grounded true}}))

(defonce renderer (js/PIXI.autoDetectRenderer.
                   (- js/window.innerWidth 5)
                   (- js/window.innerHeight 5)
                   #js {"antialias" true
                        "autoResize" true
                        "transparent" true
                        "resolution" 1
                        "clearBeforeRender" true}))

(defonce input-map {37 :left 39 :right 32 :jump})

(defn key-event [e v]
  (if-let [input (input-map (.-keyCode e))]
    (swap! app-state assoc-in [:input-keys input] v)))

(defn player-collision [{player-x :x player-y :y} {floor-x :x floor-y :y}]
  (when (and (> player-x (- floor-x 16))
             (< player-x (+ floor-x 32))
             (> player-y (+ floor-y 12))
             (< player-y (+ floor-y 16)))
    (swap! app-state assoc-in [:player :y] (+ floor-y 16))
    (swap! app-state assoc-in [:player :dy] 0)
    (swap! app-state assoc-in [:player :grounded] true)))

(defn process-input []
  (let [{{:keys [left right jump]} :input-keys
         {:keys [dy y grounded]} :player} @app-state]
    (if left (swap! app-state update-in [:player :x] #(- % 1.7)))
    (if right (swap! app-state update-in [:player :x] #(+ % 1.7)))

    (when (and jump grounded)
      (swap! app-state update-in [:player :dy] #(+ % 6))
      (swap! app-state assoc-in [:player :grounded] false))
    (swap! app-state update-in [:player :y] #(+ % dy))
    (swap! app-state update-in [:player :dy] #(if-not (= 0 %) (- % 0.4) %)))

  (doseq [floor (:world @app-state)]
    (player-collision (:player @app-state) floor)))

(defn generate-world []
  (doall
   (for [x (range 100)]
     (swap! app-state
            update
            :world
            #(conj % {:type :floor :x (* x 16) :y 84})))))

(defonce listeners
  [(.addEventListener js/window "keydown" #(key-event % true))
   (.addEventListener js/window "keyup" #(key-event % false))
   (generate-world)
   (.appendChild (.getElementById js/document "app") (.-view renderer))])

(defn main-loop []
  (.requestAnimationFrame js/window main-loop)
  (process-input)
  (draw/world @app-state renderer))

(defonce init-main-loop
  (.requestAnimationFrame js/window main-loop))

(defn on-js-reload []
  )
