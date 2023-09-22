(ns scicloj.note-to-test.v1.impl
  (:require [clojure.string :as string]
            [clojure.pprint :as pp]
            [clojure.tools.reader]
            [clojure.tools.reader.reader-types]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell])
  (:import (java.io File)))

(set! *warn-on-reflection* true)

(def *value-representations
  (atom []))

(defn define-value-representations!
  [representations]
  (reset! *value-representations representations)
  [:ok])

(defn represent-value [v]
  (-> (->> @*value-representations
           (map (fn [{:keys [predicate representation]}]
                  (when (predicate v)
                    {:note-to-test/representeation (representation v)})))
           (filter #(contains? % :note-to-test/representeation))
           first)
      (or {:note-to-test/representeation v})
      :note-to-test/representeation))

(defn represent-value-with-meta [v]
  (cond
    ;; handle a var or a nil
    (or (var? v)
        (nil? v))
    :var-or-nil
    ;; else
    :else
    {:value (-> v represent-value)
     :meta (-> v meta represent-value)}))

(defn begins-with? [value-or-set-of-values]
  (if (set? value-or-set-of-values)
    (fn [form]
      (and (list? form)
           (-> form first value-or-set-of-values)))
    ;; else
    (fn [form]
      (and (list? form)
           (-> form first (= value-or-set-of-values))))))

(defn clojure-source? [^File file]
  (boolean
   (and (.isFile file)
        (re-matches #".*\.clj[cx]?$" (.getName file)))))

(defn indent [code n-spaces]
  (let [whitespaces (apply str (repeat n-spaces \space))]
    (-> code
        (string/split #"\n")
        (->> (map (fn [line]
                    (str whitespaces line)))
             (string/join "\n")))))

(def is-template
  "
  (is (= (note-to-test/represent-value-with-meta
%s)
%s))")

(defn skip-form? [form]
  (-> form
      meta
      :note-to-test/skip))

(defn skip-represented-value? [represented-value]
  (= represented-value
     :note-to-test/skip))

(defn is-clause [source-code]
  (let [form (read-string source-code)
        value (try (eval form)
                   (catch Exception ex
                     (throw (ex-info "note-to-test: Exception on load-string"
                                     {:source-code source-code}
                                     ex))))
        representation (try (represent-value-with-meta value)
                            (catch Exception ex
                              (throw (ex-info "note-to-test: Exception on represent-value"
                                              {:source-code source-code}
                                              ex))))]
    (when-not (or (skip-form? form)
                  (-> representation
                      :value
                      skip-represented-value?))
      (format is-template
              (indent source-code 10)
              (-> representation
                  pp/pprint
                  with-out-str
                  (indent 7))))))

(def test-template
  "
(deftest test-everything
%s)
")

(defn test-clause [source-codes]
  (->> source-codes
       (map is-clause)
       (string/join "\n")
       (format test-template)))

(defn ->test-ns-symbol [ns-symbol]
  (format "%s-generated-test"
          (name ns-symbol)))

(defn ->test-path [test-ns-symbol]
  (-> test-ns-symbol
      name
      (string/replace #"-" "_")
      (string/replace #"\." "/")
      (->> (format "test/%s.clj"))))

(defn ->test-ns-requires [ns-symbol ns-requires require-forms]
  (let [requires (->> require-forms
                      (mapcat rest) ; remove the 'require symbol
                      (map second) ; remove the quote sign
                      (concat ns-requires))]
    (-> (concat (list
                 :require
                 '[clojure.test :refer [deftest is]])
                requires)
        pp/pprint
        with-out-str)))

(defn ->test-ns [test-ns-symbol test-ns-requires]
  (format "(ns %s\n%s)"
          test-ns-symbol
          (-> test-ns-requires
              (indent 2))))

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

(defn prepare-context [source-path]
  (try
    (load-file (str source-path))
    (catch Exception ex
      (throw (ex-info (str "note-to-test: Exception on load-file '" source-path "'")
                      {:source-path source-path}
                      ex))))
  (let [forms (read-forms source-path)
        ns-form (->> forms
                     (filter (begins-with? 'ns))
                     first)
        ns-symbol (second ns-form)
        require-forms (->> forms
                           (filter (begins-with? 'require)))
        ns-requires (some->> ns-form
                             (filter (begins-with? :require))
                             first
                             rest)
        test-ns-symbol (->test-ns-symbol ns-symbol)
        test-ns-requires (->test-ns-requires ns-symbol ns-requires require-forms)
        test-path (->test-path test-ns-symbol)
        codes-for-tests (->> forms
                             (remove (begins-with? '#{ns comment}))
                             (map (fn [form]
                                    (-> form
                                        meta
                                        :source)))
                             (remove nil?))]
    {:ns-symbol ns-symbol
     :ns-requires ns-requires
     :test-ns-symbol test-ns-symbol
     :test-ns-requires test-ns-requires
     :test-path test-path
     :codes-for-tests codes-for-tests}))


(defn write-tests! [context options]
  (let [{:keys [verbose accept]} options
        {:keys [ns-symbol
                ns-requires
                test-ns-symbol
                test-ns-requires
                test-path
                codes-for-tests]} context
        prev-file (io/file test-path)
        prev-content (when (.exists prev-file)
                       (slurp prev-file))
        content (str (->test-ns test-ns-symbol
                                test-ns-requires)
                     "\n"
                     (binding [*ns* (find-ns ns-symbol)]
                       (test-clause codes-for-tests)))]
    (when verbose
      (cond
        (nil? prev-content) (println "note-to-test: CREATING" test-path)
        (= content prev-content) (println "note-to-test: NO CHANGES" test-path)
        ;; TODO: perhaps print a nice diff? or give the line/column?
        (not= content prev-content) (println "note-to-test: CHANGING" test-path)))
    (when (not accept)
      (throw (ex-info "note-to-test: Changes detected with --accept false" {})))
    (io/make-parents test-path)
    (spit test-path content)
    [:wrote test-path]))
