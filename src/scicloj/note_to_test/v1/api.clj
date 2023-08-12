(ns scicloj.note-to-test.v1.api
  (:require [scicloj.note-to-test.v1.impl :as impl]))

(defn run!
  ([source-path]
   (run! source-path {}))
  ([source-path options]
   (-> source-path
       (impl/prepare-context options)
       impl/write-tests!)))

(defn define-value-representation!
  [name spec]
  (impl/define-value-representation! name spec))
