(ns advocado.styles.spacing
  (:require [garden.units :refer [px]]))

(defn margin [height dirs]
  {:margin [[(if (contains? dirs :top) (px height) 0)
             (if (contains? dirs :right) (px height) 0)
             (if (contains? dirs :bottom) (px height) 0)
             (if (contains? dirs :left) (px height) 0)]]})


