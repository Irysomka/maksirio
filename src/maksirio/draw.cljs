(ns maksirio.draw
  (:require [maksirio.sprites :as sprites :refer [mario-sprite floor-sprite]]))

(defn world [state renderer]
  (let [stage (js/PIXI.Container.)
        grid (js/PIXI.Graphics.)
        {:keys [x y]} (:player state)
        player (js/PIXI.Sprite. sprites/mario-sprite)]

    (doseq [x (map #(* 100 %) (range 10))
            y (map #(* 100 %) (range 10))]
      (doto grid
        (.lineStyle 0.2 0xCCCCCC 1)
        (.drawRect x y (+ x 100) (+ y 100))))
    (.addChild stage grid)

    (set! (.-position stage) (js/PIXI.Point. 0 (/ (.-height renderer) (.-resolution renderer))))
    (set! (.-scale stage) (js/PIXI.Point. 2 -2))

    (set! (.-x player) x)
    (set! (.-y player) y)
    (.addChild stage player)
    (doall (map
            #(let [{:keys [x y]} %
                   floor (js/PIXI.Sprite. sprites/floor-sprite)]
               (set! (.-x floor) x)
               (set! (.-y floor) y)
               (.addChild stage floor))
            (filter #(= :floor (:type %)) (:world state))))
    (.render renderer stage)))
