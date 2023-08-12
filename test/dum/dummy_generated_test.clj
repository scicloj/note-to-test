(ns dum.dummy-generated-test
  (:require
   [clojure.test :refer [deftest is]]
   [dum.dummy :refer :all]
   [scicloj.note-to-test.v1.api :as note-to-test]
   [tablecloth.api :as tc]))

(deftest test-0
  (is (=
    (note-to-test/define-value-representation!
      "tablecloth dataset"
      {:predicate tc/dataset?
       :representation (fn [ds]
                         `(tc/dataset ~(-> ds
                                           (update-vals vec)
                                           (->> (into {})))))})
    ;; =>
    :ok)))


(deftest test-1
  (is (=
    (+ 4
       5
       6)
    ;; =>
    15)))


(deftest test-2
  (is (=
    (f 11)
    ;; =>
    20)))


(deftest test-3
  (is (=
    (-> {:x [1 2 3]}
        tc/dataset
        (tc/map-columns :y [:x] (partial * 10)))
    ;; =>
    (tablecloth.api/dataset {:x [1 2 3], :y [10 20 30]}))))
