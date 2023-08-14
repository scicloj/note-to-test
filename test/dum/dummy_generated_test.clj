(ns dum.dummy-generated-test
  (:require
   [clojure.test :refer [deftest is]]
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

(defn f [x]
  (+ x 19))

(deftest test-3
  (is (=
    (f 12)
    ;; =>
    31)))


(deftest test-4
  (is (=
    (-> {:x [1 2 3]}
        tc/dataset
        (tc/map-columns :y [:x] (partial * 10)))
    ;; =>
    (tablecloth.api/dataset {:x [1 2 3], :y [10 20 30]}))))


(deftest test-5
  (is (=
    (f 13)
    ;; =>
    32)))
