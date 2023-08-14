(ns dum.dummy
  (:require [scicloj.note-to-test.v1.api :as note-to-test]
            [tablecloth.api :as tc]))

(note-to-test/define-value-representation!
  "tablecloth dataset"
  {:predicate tc/dataset?
   :representation (fn [ds]
                     `(tc/dataset ~(-> ds
                                       (update-vals vec)
                                       (->> (into {})))))})

(+ 4
   5
   6)


(defn f [x]
  (+ x 19))

(f 12)


(-> {:x [1 2 3]}
    tc/dataset
    (tc/map-columns :y [:x] (partial * 10)))

(f 13)


(comment
  (note-to-test/gentest! "notebooks/dum/dummy.clj")
  ,)
