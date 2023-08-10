(ns dum.dummy
  (:require [scicloj.note-to-test.v1.api :as note-to-test]))


(+ 1 2 3)

(+ 4
   5
   6)

9


(defn f [x]
  (+ x 9))

(f 11)

(comment
  (+ 8 7
     11)

  (-> 9
      (+ 20)))


(+ 1 3)

(f 1020)

(comment
  (note-to-test/run! "notebooks/dum/dummy.clj")
  ,)
