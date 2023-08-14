# note-to-test

Generating tests from Clojure notes

[![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/note-to-test.svg)](https://clojars.org/org.scicloj/note-to-test)

# TODO rewrite this README for the snapshot-based approach

## Status
Initial draft

## Intro

This is a tiny library for testable-documentation / literate-testing in Clojure.

It can automatically generate tests:
- from code examples in namespaces - already supported
- from code examples in docstrings - coming soon

Tests are created by running code examples and remembering their outputs. The person writing the documentation is responsible for checking that these outputs are sensible. The tests are responsible for checking these outputs remain the same on future versions.

Tests are written in standard clojure.test files. 

Each namespace of code examples has its own generated test file. 
* The top-level forms in the test namespace correspond to all runnable top-level forms in the source namespace. That is, all top-level forms except for Rich `(comment ...)` blocks. 
* The namespace definition form is adapted to the test namespace's needs. 
* Forms that result in a var (e.g., `def`, `defn`, `defonce`) are kept as-is. 
* All other forms are turned in to `(deftest ...)` test definitions.

## Usage

### REPL

```clj
(require '[scicloj.note-to-test.v1.api :as note-to-test])
```

Assume you have a namespace, say `dum.dummy` in the file [notebooks/dum/dummy.clj](notebooks/dum/dummy.clj), which has some code examples in it.

```clj
(note-to-test/gentest! "notebooks/dum/dummy.clj")
```
would generate a test namespace 'dum.dummy-generated-test' in the file [test/dum/dummy_generated_test.clj](test/dum/dummy_generated_test.clj) with clojure.test tests verifying that those code examples actually return the values they had at the time we executed that `gentest!` call.

### Command Line

See [scicloj.note-to-test.v1.main/-main](src/scicloj/note_to_test/v1/main)

For a deps.edn project, merge the following alias

```clojure
{:aliases
 {:gen {:extra-deps {org.scicloj/note-to-test {:mvn/version "RELEASE"}}
        :main-opts ["-m" "scicloj.note-to-test.v1.main"]}}}
```

And then invoke it from the command line

```sh
clojure -M:dev:gen --verbose
```

### Build.tools

Add `gentests!` to your test function in `build.clj`.

### Handling special values

For plain Clojure data structures, pretty printing the structure results in a readable string that can be used for the test code.

Other values may need some care to be represented in the test. Defining such representations can be done using `define-value-representation!`.

For example, let us add support for [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) datasets, that we will use through [Tablecloth](https://scicloj.github.io/tablecloth/).

```clj
(require '[tablecloth.api :as tc])
(note-to-test/define-value-representation!
  "tech.ml.dataset dataset"
  {:predicate tc/dataset?
   :representation (fn [ds]
                     `(tc/dataset ~(-> ds
                                       (update-vals vec)
                                       (->> (into {})))))})
```

Now, a code example like
```clj
(-> {:x [1 2 3]}
    tc/dataset
    (tc/map-columns :y [:x] (partial * 10)))
```
will result in a test like
```clj
(deftest test-3
  (is (=
       (-> {:x [1 2 3]}
           tc/dataset
           (tc/map-columns :y [:x] (partial * 10)))
       ;; =>
       (tablecloth.api/dataset {:x [1 2 3], :y [10 20 30]}))))
```

You see, the output value is represented as a code snippet that would generate that value. Since `tech.ml.dataset` datasets can be compared using the `=` function, this is a valid test for our code example.

## Wishlist
- support running from command line
- support an alternative plain-data output format to help seeing the output changes explicitly
- make clear error messages:
  - when outputs change
  - when the code fails
- support code examples in comment blocks?
- support metadata to skip certain forms
- remove nil outputs?
- support docstrings
  - generate tests from code examples in docstrings
  - explore generate docstrings in a structured way (marking code examples explicitly)
- support approximate comparisons of values for floating-point computations
- support automatic regeneration (say, using file watch)

## License

Copyright © 2023 Scicloj

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
