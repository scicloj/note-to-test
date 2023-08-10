(ns scicloj.note-to-test.v1.impl
  (:require [clojure.string :as string]
            [clojure.pprint :as pp]
            [clojure.tools.reader]
            [clojure.tools.reader.reader-types]
            [clojure.java.io :as io]))



(def *special-value-representations
  (atom []))

(defn represent-value [v]
  (-> (->> @*special-value-representations
           (map (fn [{:keys [pred repr]}]
                  (if (pred v)
                    (repr v))))
           (filter identity)
           first)
      (or v)))

(defn indent [code]
  (-> code
      (string/split #"\n")
      (->> (map (partial str "    "))
           (string/join "\n"))))

(def test-template
  "
(deftest %s
  (is (=
%s

%s)))
")

(defn generate-test [code index source-ns]
  (format test-template
          (str "test-" index)
          (indent code)
          (binding [*ns* source-ns]
            (-> code
                read-string
                eval
                represent-value
                pp/pprint
                with-out-str
                indent))))

(defn test-ns-symbol [ns-symbol]
  (-> ns-symbol
      name
      (str "-generated-test")
      symbol))

(defn generate-test-ns [ns-symbol]
  (-> (list 'ns
            (test-ns-symbol ns-symbol)
            (list
             :require
             '[clojure.test :refer [deftest is]]
             [ns-symbol :refer :all]))
      pp/pprint
      with-out-str))

(defn code->forms [code]
  (->> code
       clojure.tools.reader.reader-types/source-logging-push-back-reader
       repeat
       (map #(clojure.tools.reader/read % false ::EOF))
       (take-while (partial not= ::EOF))))

(defn read-forms [source-path]
  (->> source-path
       slurp
       code->forms))

(defn begins-with? [symbol-or-symbols-set]
  (cond
    ;; a symbol
    (symbol? symbol-or-symbols-set)
    (fn [form]
      (and (list? form)
           (-> form first (= symbol-or-symbols-set))))
    ;; a set of symbols
    (set? symbol-or-symbols-set)
    (fn [form]
      (and (list? form)
           (-> form first symbol-or-symbols-set)))))

(defn ns-name->test-path [ns-name]
  (-> ns-name
      name
      (string/replace #"\." "/")
      (->> (format "test/%s_generated_test.clj"))))


(defn test-form->original-form [test-form]
  (-> test-form
      meta
      :source
      (string/split #"\n")
      (->> (drop 2)
           (string/join "\n"))
      code->forms
      first))


(defn read-tests [test-path]
  (when (-> test-path
            io/file
            (.exists))
    (->> test-path
         read-forms
         (filter (begins-with? 'deftest))
         (map (fn [test-form]
                {:test-form test-form
                 :original-form (test-form->original-form
                                 test-form)})))))


(defn prepare-context [source-path {:keys [cleanup-existing-tests?]}]
  (load-file source-path)
  (let [forms ( read-forms
               source-path)
        ns-form (->> forms
                     (filter (begins-with? 'ns))
                     first)
        ns-symbol (second ns-form)
        test-path (ns-name->test-path
                   ns-symbol)
        _ (when cleanup-existing-tests?
            (-> test-path
                io/file
                io/delete-file))
        existing-tests (read-tests test-path)
        known-forms (some->> existing-tests
                             (map :original-form)
                             set)
        codes-for-tests (->> forms
                             (filter (complement
                                      (begins-with?
                                       '#{ns
                                          def
                                          defonce
                                          defn
                                          comment})))
                             (filter (-> known-forms
                                         (or #{})
                                         complement))
                             (map (fn [form]
                                    (-> form
                                        meta
                                        :source)))
                             (remove nil?))]
    {:ns-symbol ns-symbol
     :test-path test-path
     :existing-tests existing-tests
     :codes-for-tests codes-for-tests}))


(defn write-tests! [context]
  (let [{:keys [ns-symbol
                test-path
                codes-for-tests
                existing-tests]} context]
    (when-not existing-tests
      (io/make-parents test-path))
    (let [n-existing-tests (count existing-tests)]
      (->> codes-for-tests
           (map-indexed (fn [i code]
                          (-> code
                              (generate-test (+ i n-existing-tests)
                                             (find-ns ns-symbol))
                              println
                              with-out-str)))
           (concat
            (if existing-tests
              []
              [(generate-test-ns ns-symbol)]))
           (string/join "\n")
           (#(spit test-path
                   %
                   :append true))))
    [:wrote test-path]))
