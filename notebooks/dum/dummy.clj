(ns dum.dummy
  (:require [scicloj.note-to-test.v1.api :as note-to-test]
            [tablecloth.api :as tc]))

(note-to-test/define-value-representations!
  [{:predicate symbol?
    :representation (partial str "symbol ")}
   {:predicate tc/dataset?
    :representation (fn [ds]
                      (-> ds
                          (update-vals vec)
                          (->> (into {}))))}
   {:predicate (partial = 5)
    :representation (constantly :five)}
   {:predicate map?
    :representation (fn [m]
                      (-> m
                          (update-keys note-to-test/represent-value)
                          (update-vals note-to-test/represent-value)))}
   {:predicate sequential?
    :representation (partial mapv note-to-test/represent-value)}])

(+ 2 3)

{:x (+ 2 3)}

(+ 4
   5
   6)

^:note-to-test/skip
(+ 1 2)

(defn f [x]
  (+ x 19))

(f 12)

(-> {:x [1 2 3]}
    tc/dataset
    (tc/map-columns :y [:x] (partial * 10)))

(f 13)

{:x 9}

(comment
  (note-to-test/gentest! "notebooks/dum/dummy.clj"
                         {:accept true
                          :verbose true})
  ,)
