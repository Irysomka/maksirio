(ns maksirio.sprites)

(def mario-spritesheet
  (.fromImage js/PIXI.Texture "images/mario_sprites.gif" ))

(def mario-sprite
  (js/PIXI.Texture. mario-spritesheet
                    (js/PIXI.Rectangle. 23 507 13 16)))

(def floor-sprite
  (js/PIXI.Texture. mario-spritesheet
                    (js/PIXI.Rectangle. 373 124 16 16)))
