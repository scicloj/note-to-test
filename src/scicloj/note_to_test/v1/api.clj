(ns scicloj.note-to-test.v1.api
  (:require [clojure.java.io :as io]
            [scicloj.note-to-test.v1.impl :as impl])
  (:import (java.io File)))

(set! *warn-on-reflection* true)

(defn gentest!
  "Generate a clojure.test file for the code examples in the file at `source-path`.

  Example:
  ```clj
  (gentest! \"notebooks/dum/dummy.clj\")
  ```
  "
  [source-path options]
  (-> source-path
      impl/prepare-context
      (impl/write-tests! options)))

(defn gentests!
  "Generate tests for all source files discovered in [dirs]."
  ([dirs] (gentests! dirs {}))
  ([dirs options]
   (let [{:keys [verbose]} options]
     (when verbose
       (println "Generating tests with options:" (pr-str options)))
     (doseq [dir dirs
             ^File file (file-seq (io/file dir))
             :when (impl/clojure-source? file)]
       (when verbose (println "Loading file" (str file)))
       (cond-> (gentest! file options)
               verbose (println))))
   [:success]))

(defn define-value-representation!
  "Define a data representation for special values. Outputs in test code will be represented this way.

  For example, let us add support for [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) datasets, that we will use through [Tablecloth](https://scicloj.github.io/tablecloth/).
  ```clj
  (require '[tablecloth.api :as tc])
  (note-to-test/define-value-representation!
    \"tech.ml.dataset dataset\"
    {:predicate tc/dataset?
     :representation (fn [ds]
                       `(tc/dataset ~(-> ds
                                         (update-vals vec)
                                         (->> (into {})))))})
  ```
  "
  [name spec]
  (impl/define-value-representation! name spec))


(defn represent-value
  "Represent a given value `v` using the extensible definitions of special value representations."
  [v]
  (impl/represent-value v))
