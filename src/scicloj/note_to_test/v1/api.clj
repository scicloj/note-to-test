(ns scicloj.note-to-test.v1.api
  (:require [scicloj.note-to-test.v1.impl :as impl]))

(defn prepare-context [source-path]
  (impl/prepare-context source-path))

(defn write-tests! [context]
  (impl/write-tests! context))
