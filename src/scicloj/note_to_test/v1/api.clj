(ns scicloj.note-to-test.v1.api
  (:require [clojure.java.io :as io]
            [scicloj.note-to-test.v1.impl :as impl])
  (:import (java.io File)))

(set! *warn-on-reflection* true)

(defn gentest!
  "Generate a clojure.test file for the code examples in the file at `source-path`.

  Options:
  - `accept` - boolean - default `false` - should we accept overriding an existing test file which has changed?
  - `verbose` - boolean - default `false` - should we report whether an existing test file has changed?

  Examples:
  ```clj
  (gentest! \"notebooks/dum/dummy.clj\")
  ```
  ```clj
  (gentest! \"notebooks/dum/dummy.clj\"
            {:accept true
             :verbose true})
  ```
   "
  [source-path options]
  (-> source-path
      impl/prepare-context
      (impl/write-tests! options)))

(defn gentests!
  "Generate tests for all source files discovered in [dirs].

  Options: like in `gentest!`.

  Examples:
  ```clj
  (gentests! [\"notebooks\"])
  ```
  ```clj
  (gentests! [\"notebooks\"]
            {:accept true
             :verbose true})
  ```
  "
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

(defn define-value-representations!
  "Define how values should be represented in the tests.
  The definition is a vector of maps, where each map has a predicate and a representation function. Each value is checked through the maps in order and represented by the representation function corresponding to the first predicate that applies to it.

  Example:
  ```clj
  (define-value-representations!
    [{:predicate #(> % 20)
      :representation (partial * 100)}
     {:predicate #(> % 10)
      :representation (partial * 10)}])

  (represent-value 9) ; => 9, no predicate applies
  (represent-value 19) ; => 190, second predicate applies
  (represent-value 29) ; => 2900, first predicate applies
  ```
  "
  [representations]
  (impl/define-value-representations! representations))

(defn represent-value
  "Represent a given value `v` using the extensible definitions of special value representations."
  [v]
  (impl/represent-value v))

(defn represent-value-with-meta
  "Represent a given value `v`, apply `represent-value` to both `v` and its metadata, and retorn both in one data structure."
  [v]
  (impl/represent-value-with-meta v))
