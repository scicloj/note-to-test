(ns dum.dummy-generated-test
  (:require
   [clojure.test :refer [deftest is]]
   [scicloj.note-to-test.v1.api :as note-to-test]
   [tablecloth.api :as tc]))

(deftest test-everything

  (is (= (note-to-test/represent-value-with-meta
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
              :representation (partial mapv note-to-test/represent-value)}]))
       {:value [:ok], :meta nil}))

  (is (= (note-to-test/represent-value-with-meta
          (+ 2 3))
       {:value :five, :meta nil}))

  (is (= (note-to-test/represent-value-with-meta
          {:x (+ 2 3)})
       {:value {:x :five}, :meta nil}))

  (is (= (note-to-test/represent-value-with-meta
          (+ 4
             5
             6))
       {:value 15, :meta nil}))


  (is (= (note-to-test/represent-value-with-meta
          (defn f [x]
            (+ x 19)))
       :var-or-nil))

  (is (= (note-to-test/represent-value-with-meta
          (f 12))
       {:value 31, :meta nil}))

  (is (= (note-to-test/represent-value-with-meta
          (-> {:x [1 2 3]}
              tc/dataset
              (tc/map-columns :y [:x] (partial * 10))))
       {:value {:x [1 2 3], :y [10 20 30]}, :meta {:name "_unnamed"}}))

  (is (= (note-to-test/represent-value-with-meta
          (f 13))
       {:value 32, :meta nil}))

  (is (= (note-to-test/represent-value-with-meta
          {:x 9})
       {:value {:x 9}, :meta nil})))
