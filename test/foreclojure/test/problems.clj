(ns foreclojure.test.problems
  (:use [foreclojure.problems])
  (:use [clojure.test])
  (:use [midje.sweet]))

(let [code "(I AM SOME COOL CODE 111)"
      commented-code (str "(I AM SOME ;REALLY\n"
                          "   COOL,   ;COOL\n"
                          "   CODE 111)")
      internal-semicolon "(do \"s;s\" \\; (fn [coll] ((fn dist [prev coll] (lazy-seq (when-let [[x & xs] (seq coll)] (let [more (dist (conj prev x) xs)] (if (contains? prev x) more (cons x more)))))) #{} coll))) ;plus a comment :)"
      anonymous-function "reduce #(update-in % [%2] (fnil inc 0)) {}"]
  (deftest test-code-length
    (fact "code-length counts characters in code"
      (code-length code) => 20)
    (fact "code-length can handle comments"
      (code-length commented-code) => 20)
    (fact "code-length can handle escaped semicolons"
      (code-length internal-semicolon) => 150)
    (fact "code-length can handle anonymous functions"
      (code-length anonymous-function) => 35)
    (fact "code-length can handle empty data structures"
      (code-length "[{} #{} [] () \"\"]") => 13)
    (fact "code-length can handle metadata"
      (code-length "^String ^dynamic ^{:key :value} ^:test") => 34)
    (fact "code-length can handle unquotes"
      (code-length "`(1 2 ~(list 3 4) ~@(list 3 4))") => 24)
    (fact "code-length can handle regexes"
      (code-length ".split #\"\\n\"") => 11)
    (fact "code-length can handle quotes"
      (code-length "'() #'var") => 8)
    (fact "code-length can handle derefs"
      (code-length "@form") => 5)
    (fact "code-length doesn't simplify literals"
      (code-length "1.00 10/5") => 8)
    (fact "code-length can handle nils"
      (code-length "nil (nil)") => 8)
    (fact "code-length can handle double-quote shenanigans"
      (code-length "\";\\\";\\\";\"\";\\\";\";\";\\\";\"") => 15
      (code-length "\";\\\";\\\";\", \";\\\";\", ;\";\\\";\"") => 15
      (code-length "\"\\\";\";test\"comment;\"\\\"") => 5)
    (fact "code-length can handle multi-line strings"
      (code-length "\";
;\" ;newline at cost of 1") => 5
      (code-length "\";\\n;\" ;newline at cost of 2") => 6)
    (fact "code-length doesn't double count dos line-endings"
      (code-length "\";\r\n;\"") => (count "\";
;\"")
      (code-length "\";\r\n;\"") => 5)
    (fact "code-length counts #_" ;probably not worth fixing
      (code-length "#_ hello") => 7)))
