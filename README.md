# note-to-test

Generating tests from Clojure notes

[![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/note-to-test.svg)](https://clojars.org/org.scicloj/note-to-test)

## Status
Experiental. Please expect breaking changes.

## Intro

This is a tiny library for testable-documentation / literate-testing in Clojure.

It can automatically generate tests:
- from code examples in namespaces - already supported
- from code examples in docstrings - coming soon 

Tests are created by running code examples and remembering their outputs. The person writing the documentation is responsible for checking that these outputs are sensible. The tests are responsible for checking these outputs remain the same on future versions.

### Test files
Tests are written in standard `clojure.test` files. Each namespace of code examples has its own generated test file. 
* The test namespace has one `(deftest ...)` clause with many `is` clauses.
* The `is` clauses corresond to all runnable top-level forms in the source namespace. That is, all top-level forms except for the `(ns ...)` definition and Rich `(comment ...)` blocks. 
* The namespace definition form is adapted to the test namespace's needs, including not only the source namespace `require`s, but also those which appear in the body of the source namespace.

## Usage

### REPL

```clj
(require '[scicloj.note-to-test.v1.api :as note-to-test])
```

Assume you have a namespace, say `dum.dummy` in the file [notebooks/dum/dummy.clj](notebooks/dum/dummy.clj), which has some code examples in it.

```clj
(note-to-test/gentest! "notebooks/dum/dummy.clj")
```
would generate a test namespace 'dum.dummy-generated-test' in the file [test/dum/dummy_generated_test.clj](test/dum/dummy_generated_test.clj) with checks verifying that those code examples actually return the values they had at the time we executed that `gentest!` call.

The call to `gentest!` can also accept an options map.
Options:
- `accept` - boolean - default `false` - should we accept overriding an existing test file which has changed?
- `verbose` - boolean - default `false` - should we report whether an existing test file has changed?

E.g.:
```clj
(note-to-test/gentest! "notebooks/dum/dummy.clj"
                       {:accept true
                        :verbose true})
```

There is also a `gentests!` function that handles a list of directories, rather than one source file.

### Command Line

See [scicloj.note-to-test.v1.main/-main](src/scicloj/note_to_test/v1/main).

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

For plain Clojure vectors, maps, sets, and primitive values, pretty printing the structure results in a readable string that can be used for the test code as is.

Other values may need some care to be represented in the test. Defining such representations can be done using `define-value-representations!`. Applying the data representations is done internally by the library, but can also be done directly using `represent-value`. This is useful for defining recursive representations.

The definition is a vector of maps, where each map has a predicate and a representation function. Each value is checked through the maps in order and represented by the representation function corresponding to the first predicate that applies to it.

Toy example:
```clj
(note-to-test/define-value-representations!
  [{:predicate #(> % 20)
    :representation (partial * 100)}
   {:predicate #(> % 10)
    :representation (partial * 10)}])

(note-to-test/represent-value 9) ; => 9, no predicate applies
(note-to-test/represent-value 19) ; => 190, second predicate applies
(note-to-test/represent-value 29) ; => 2900, first predicate applies
```

For a more practical example, let us define our representations so that all values and keys of a map are represented recursively, and that long sequential structures are shortened (to keep the test file not too large) and turned into vectors (to make the printed value valid to evaluate).

```clj
(note-to-test/define-value-representations!
  [{:predicate map?
    :representation (fn [m]
                      (-> m
                          (update-keys represent-value)
                          (update-vals represent-value)))}
   {:predicate sequential?
    :representation (fn [v]
                      (->> v
                           (take 5)
                           vec))}])

(note-to-test/represent-value
 {:x (range 99)
  :y 9}) ; => {:x [0 1 2 3 4], :y 9}
```
### Skipping tests
Tesks will be skipped if either original form has the `^:note-to-test/skip` metadata, e.g.

```clj
^:note-to-test/skip
(+ 2 3) ; will be skipped
```
or the represented value is `:note-to-test/skip`, e.g.
```clj
(note-to-test/define-value-representations!
  [{:predicate #{5}
    :representation (constantly :note-to-test/skip)}])

(+ 2 3) ; will be skipped
```

## Wishlist
- generate tests from code examples in docstrings
- explore generate docstrings in a structured way (marking code examples explicitly)
- support an alternative plain-data output format to help seeing the output changes explicitly
- make clearer error messages
  - when outputs change
  - when the code fails
- support code examples in comment blocks?
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

