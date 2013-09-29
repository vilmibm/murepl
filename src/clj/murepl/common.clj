(ns murepl.common)

(def directions [:north :south :west :east :up :down])

(defn valid-direction? [direction]
  (some #{direction} directions))

(defn opposite-dir [direction]
  (case direction
    :north :south
    :south :north
    :east :west
    :west :east
    :up :down
    :down :up))

(defn pretty-came-from [direction]
  (case direction
    :north "the north"
    :south "the south"
    :east "the east"
    :west "the west"
    :up "above"
    :down "below"))
