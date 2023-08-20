(ns dum.dummy-generated-test
  (:require
   [clojure.test :refer [deftest is]]
   [scicloj.note-to-test.v1.api :as note-to-test]
   [tablecloth.api :as tc]
   clojure.java.io))

(deftest test-everything


  (is (= (note-to-test/represent-value
          (+ 2 3))
       :five))


  (is (= (note-to-test/represent-value
          (+ 4
             5
             6))
       15))



  (is (= (note-to-test/represent-value
          (defn f [x]
            (+ x 19)))
       :var))


  (is (= (note-to-test/represent-value
          (f 12))
       31))


  (is (= (note-to-test/represent-value
          (require 'clojure.java.io))
       nil))


  (is (= (note-to-test/represent-value
          (-> {:x [1 2 3]}
              tc/dataset
              (tc/map-columns :y [:x] (partial * 10))))
       {:x [1 2 3], :y [10 20 30]}))


  (is (= (note-to-test/represent-value
          (f 13))
       32))


  (is (= (note-to-test/represent-value
          {:x 9})
       {:x 9}))
)
