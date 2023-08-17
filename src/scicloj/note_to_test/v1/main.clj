(ns scicloj.note-to-test.v1.main
  (:require [clojure.tools.cli :as cli]
            [scicloj.note-to-test.v1.api :as ntt]))

(def cli-options
  [["-d" "--dirs" :default ["notebooks"]]
   ["-a" "--accept" :default true]
   ["-v" "--verbose"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [options summary]} (cli/parse-opts args cli-options)
        {:keys [dirs help]} options]
    (if help
      (println summary)
      (ntt/gentests! dirs options))))
