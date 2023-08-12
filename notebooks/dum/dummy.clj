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
  (+ x 9))

(f 11)

(-> {:x [1 2 3]}
    tc/dataset
    (tc/map-columns :y [:x] (partial * 10)))

(comment
  (note-to-test/run! "notebooks/dum/dummy.clj")
  (note-to-test/run! "notebooks/dum/dummy.clj"
                     {:cleanup-existing-tests? true})
  ,)
