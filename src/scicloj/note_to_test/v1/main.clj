(ns scicloj.note-to-test.v1.main
  (:require [clojure.edn :as edn]
            [clojure.tools.cli :as cli]
            [scicloj.note-to-test.v1.api :as ntt]))

(def cli-options
  [["-d" "--dirs" :default ["notebooks"]]
   ["-a" "--accept"]
   ["-c" "--cleanup-existing-tests?"]
   ["-v" "--verbose"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [options summary]} (cli/parse-opts args cli-options)
        {:keys [exit-message ok? dirs]} options]
    (if exit-message
      (do (println exit-message)
          (System/exit (if ok? 0 1)))
      (ntt/gentests! dirs options))))
