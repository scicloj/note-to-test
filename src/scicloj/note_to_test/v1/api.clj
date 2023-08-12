(ns scicloj.note-to-test.v1.api
  (:require [scicloj.note-to-test.v1.impl :as impl]))

(defn run!
  "Generate a clojure.test file for the code examples in the file at `source-path`.
  Optionsl `options`:
  - :cleanup-existing-tests? - boolean - default `false` - should we create the tests file from scratch (when `true`), or incrementally (when `false`)?

  Examples:
  Generate tests for a given file incrementally, handling only new code examples.
  ```clj
  (run! \"notebooks/dum/dummy.clj\")
  ```
  Generate tests from scratch:
  ```clj
  (run! \"notebooks/dum/dummy.clj\"
        {:cleanup-existing-tests? true})
  ```
  "
  ([source-path]
   (run! source-path {}))
  ([source-path options]
   (-> source-path
       (impl/prepare-context options)
       impl/write-tests!)))

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
