(ns foreclojure.data-set
  (:use [somnium.congomongo]))

(defn load-problems []
  (do
    (insert! :seqs
             {:_id "problems"
              :seq 87})
    (insert! :problems
             {:_id 1
              :title "Nothing but the Truth"
              :times-solved 0
              :description "This is a clojure form.  Enter a value which will make the form evaluate to true.  Don't over think it!  If you are confused, see the <a href='/directions'>getting started</a> page.  Hint: true is equal to true."
              :tags ["elementary"]
              :approved true
              :tests ["(= __ true)"]})

    (insert! :problems
             {:_id 2
              :title "Simple Math"
              :times-solved 0
              :description "If you are not familiar with <a href='http://en.wikipedia.org/wiki/Polish_notation'>polish notation</a>, simple arithmetic might seem confusing."
              :tags ["elementary"]
              :approved true
              :tests ["(= (- 10 (* 2 3)) __)"]})

    (insert! :problems
             {:_id 3
              :title "Intro to Strings"
              :times-solved 0
              :description "Clojure strings are Java strings.  This means that you can use any of the Java string methods on Clojure strings."
              :tags["elementary"]
              :approved true
              :tests ["(= __ (.toUpperCase \"hello world\"))"]})


    (insert! :problems
             {:_id 4
              :title "Intro to Lists"
              :times-solved 0
              :description "Lists can be constructed with either a function or a quoted form."
              :tags["elementary"]
              :approved true
              :tests ["(= (list __) '(:a :b :c))"]})

    (insert! :problems
             {:_id 5
              :title "Lists: conj"
              :times-solved 0
              :description "When operating on a list, the conj function will return a new list with one or more items \"added\" to the front."
              :tags["elementary"]
              :approved true
              :tests ["(= __ (conj '(2 3 4) 1))"
                      "(= __ (conj '(3 4) 2 1))"]})

    (insert! :problems
             {:_id 6
              :title "Intro to Vectors"
              :times-solved 0
              :description "Vectors can be constructed several ways.  You can compare them with lists.<br/><br/>Note: the brackets [] surrounding the blanks __ are part of the test case."
              :tags["elementary"]
              :approved true
              :tests ["(= [__] (list :a :b :c) (vec '(:a :b :c)) (vector :a :b :c))"]})

    (insert! :problems
             {:_id 7
              :title "Vectors: conj"
              :times-solved 0
              :description "When operating on a Vector, the conj function will return a new vector with one or more items \"added\" to the end."
              :tags["elementary"]
              :approved true
              :tests ["(= __ (conj [1 2 3] 4))"
                      "(= __ (conj [1 2] 3 4))"]})


    (insert! :problems
             {:_id 8
              :title "Intro to Sets"
              :times-solved 0
              :description "Sets are collections of unique values."
              :tags["elementary"]
              :approved true
              :tests ["(= __ (set '(:a :a :b :c :c :c :c :d :d)))"
                      "(= __ (clojure.set/union #{:a :b :c} #{:b :c :d}))"]})

    (insert! :problems
             {:_id 9
              :title "Sets: conj"
              :times-solved 0
              :description "When operating on a set, the conj function returns a new set with one or more keys \"added\"."
              :tags["elementary"]
              :approved true
              :tests ["(= #{1 2 3 4} (conj #{1 4 3} __))"]})


    (insert! :problems
           {:_id 10
            :title "Intro to Maps"
            :times-solved 0
            :description "Maps store key-value pairs.  Both maps and keywords can be used as lookup functions. Commas can be used to make maps more readable, but they are not required."
            :tags["elementary"]
            :approved true
            :tests ["(= __ ((hash-map :a 10, :b 20, :c 30) :b))"
                    "(= __ (:b {:a 10, :b 20, :c 30}))"]})

    (insert! :problems
             {:_id 11
              :title "Maps: conj"
              :times-solved 0
              :description "When operating on a map, the conj function returns a new map with one or more key-value pairs \"added\"."
              :tags["elementary"]
              :approved true
              :tests ["(= {:a 1, :b 2, :c 3} (conj {:a 1} __ [:c 3]))"]})

    (insert! :problems
             {:_id 12
              :title "Intro to Sequences"
              :times-solved 0
              :description "All Clojure collections support sequencing.  You can operate on sequences with functions like first, second, and last."
              :tags["elementary"]
              :approved true
              :tests ["(= __ (first '(3 2 1)))"
                      "(= __ (second [2 3 4]))"
                      "(= __ (last (list 1 2 3)))"]})

    (insert! :problems
             {:_id 13
              :title "Sequences: rest"
              :times-solved 0
              :description "The rest function will return all the items of a sequence except the first."
              :tags["elementary"]
              :approved true
              :tests ["(= __ (rest [10 20 30 40]))"]})

    (insert! :problems
             {:_id 14
              :title "Intro to Functions"
              :times-solved 0
              :description "Clojure has many different ways to create functions."
              :tags["elementary"]
              :approved true
              :tests ["(= __ ((fn add-five [x] (+ x 5)) 3))"
                      "(= __ ((fn [x] (+ x 5)) 3))"
                      "(= __ (#(+ % 5) 3))"
                      "(= __ ((partial + 5) 3))"]})

    (insert! :problems
           {:_id 15
            :title "Double Down"
            :times-solved 0
            :description "Write a function which doubles a number."
            :tags ["elementary"]
            :approved true
            :tests ["(= (__ 2) 4)"
                    "(= (__ 3) 6)"
                    "(= (__ 11) 22)"
                    "(= (__ 7) 14)"]})

    (insert! :problems
             {:_id 16
              :title "Hello World"
              :times-solved 0
              :description "Write a function which returns a personalized greeting."

              :tags ["elementary"]
              :approved true
              :tests ["(= (__ \"Dave\") \"Hello, Dave!\")"
                      "(= (__ \"Jenn\") \"Hello, Jenn!\")"
                      "(= (__ \"Rhea\") \"Hello, Rhea!\")"]})

    (insert! :problems
             {:_id 17
              :title "Sequences: map"
              :times-solved 0
              :description "The map function takes two arguments: a function (f) and a sequence (s).  Map returns a new sequence consisting of the result of applying f to each item of s.  Do not confuse the map function with the map data structure."
              :tags["elementary"]
              :approved true
              :tests ["(= __ (map #(+ % 5) '(1 2 3)))"]})

    (insert! :problems
             {:_id 18
              :title "Sequences: filter"
              :times-solved 0
              :description "The filter function takes two arguments: a predicate function (f) and a sequence (s).  Filter returns a new sequence consisting of all the items of s for which (f item) returns true."
              :tags["elementary"]
              :approved true
              :tests ["(= __ (filter #(> % 5) '(3 4 5 6 7)))"]})

    (insert! :problems
             {:_id 19
              :title "Last Element"
              :times-solved 0
              :restricted ["last"]
              :description "Write a function which returns the last element in a sequence."
              :tags ["easy" "seqs" "core-functions"]
              :approved true
              :tests ["(= (__ [1 2 3 4 5]) 5)"
                      "(= (__ '(5 4 3)) 3)"
                      "(= (__ [\"b\" \"c\" \"d\"]) \"d\")"]})

    (insert! :problems
             {:_id 20
              :title "Penultimate Element"
              :times-solved 0
              :description "Write a function which returns the second to last element from a sequence."
              :tags["easy" "seqs"]
              :approved true
              :tests ["(= (__ (list 1 2 3 4 5)) 4)"
                      "(= (__ [\"a\" \"b\" \"c\"]) \"b\")"
                      "(= (__ [[1 2] [3 4]]) [1 2])"]})

    (insert! :problems
             {:_id 21
              :title "Nth Element"
              :times-solved 0
              :restricted ["nth"]
              :description "Write a function which returns the Nth element from a sequence."
              :tags["easy" "seqs" "core-functions"]
              :approved true
              :tests ["(= (__ '(4 5 6 7) 2) 6)"
                      "(= (__ [:a :b :c] 0) :a)"
                      "(= (__ [1 2 3 4] 1) 2)"
                      "(= (__ '([1 2] [3 4] [5 6]) 2) [5 6])"]})

    (insert! :problems
             {:_id 22
              :title "Count a Sequence"
              :times-solved 0
              :restricted ["count"]
              :description "Write a function which returns the total number of elements in a sequence."
              :tags["easy" "seqs" "core-functions"]
              :approved true
              :tests ["(= (__ '(1 2 3 3 1)) 5)"
                      "(= (__ \"Hello World\") 11)"
                      "(= (__ [[1 2] [3 4] [5 6]]) 3)"
                      "(= (__ '(13)) 1)"
                      "(= (__ '(:a :b :c)) 3)"]})

    (insert! :problems
             {:_id 23
              :title "Reverse a Sequence"
              :times-solved 0
              :restricted ["reverse"]
              :description "Write a function which reverses a sequence."
              :tags["easy" "seqs" "core-functions"]
              :approved true
              :tests ["(= (__ [1 2 3 4 5]) [5 4 3 2 1])"
                      "(= (__ (sorted-set 5 7 2 7)) '(7 5 2))"
                      "(= (__ [[1 2][3 4][5 6]]) [[5 6][3 4][1 2]])"]})

    (insert! :problems
             {:_id 24
              :title "Sum It All Up"
              :times-solved 0
              :description "Write a function which returns the sum of a sequence of numbers."
              :tags ["easy" "seqs"]
              :approved true
              :tests ["(= (__ [1 2 3]) 6)"
                      "(= (__ (list 0 -2 5 5)) 8)"
                      "(= (__ #{4 2 1}) 7)"
                      "(= (__ '(0 0 -1)) -1)"
                      "(= (__ '(1 10 3)) 14)"]})

    (insert! :problems
             {:_id 25
              :title "Find the odd numbers"
              :times-solved 0
              :description "Write a function which returns only the odd numbers from a sequence."
              :tags["easy" "seqs"]
              :approved true
              :tests ["(= (__ #{1 2 3 4 5}) '(1 3 5))"
                      "(= (__ [4 2 1 6]) '(1))"
                      "(= (__ [2 2 4 6]) '())"
                      "(= (__ [1 1 1 3]) '(1 1 1 3))"]})

    (insert! :problems
             {:_id 26
              :title "Fibonacci Sequence"
              :times-solved 0
              :description "Write a function which returns the first X fibonacci numbers."
              :tags["easy" "Fibonacci" "seqs"]
              :approved true
              :tests ["(= (__ 3) '(1 1 2))"
                      "(= (__ 6) '(1 1 2 3 5 8))"
                      "(= (__ 8) '(1 1 2 3 5 8 13 21))"]})

    (insert! :problems
             {:_id 27
              :title "Palindrome Detector"
              :times-solved 0
              :description "Write a function which returns true if the given sequence is a palindrome.<br/><br>
                Hint: \"racecar\" does not equal '(\\r \\a \\c \\e \\c \\a \\r)"
              :tags["easy" "seqs"]
              :approved true
              :tests ["(false? (__ '(1 2 3 4 5)))"
                      "(true? (__ \"racecar\"))"
                      "(true? (__ [:foo :bar :foo]))"
                      "(true? (__ '(1 1 3 3 1 1)))"
                      "(false? (__ '(:a :b :c)))"]})

    (insert! :problems
             {:_id 28
              :title "Flatten a Sequence"
              :times-solved 0
              :restricted ["flatten"]
              :description "Write a function which flattens a sequence."

              :tags["easy" "seqs" "core-functions"]
              :approved true
              :tests ["(= (__ '((1 2) 3 [4 [5 6]])) '(1 2 3 4 5 6))"
                      "(= (__ [\"a\" [\"b\"] \"c\"]) '(\"a\" \"b\" \"c\"))"
                      "(= (__ '((((:a))))) '(:a))"]})

    (insert! :problems
             {:_id 29
              :title "Get the Caps"
              :times-solved 0
              :description "Write a function which takes a string and returns a new string containing only the capital letters."
              :tags["easy" "strings"]
              :approved true
              :tests ["(= (__ \"HeLlO, WoRlD!\") \"HLOWRD\")"
                      "(empty? (__ \"nothing\"))"
                      "(= (__ \"$#A(*&987Zf\") \"AZ\")"]})

    (insert! :problems
             {:_id 30
              :title "Compress a Sequence"
              :times-solved 0
              :description "Write a function which removes consecutive duplicates from a sequence."
              :tags ["easy" "seqs"]
              :approved true
              :tests ["(= (apply str (__ \"Leeeeeerrroyyy\")) \"Leroy\")"
                      "(= (__ [1 1 2 3 3 2 2 3]) '(1 2 3 2 3))"
                      "(= (__ [[1 2] [1 2] [3 4] [1 2]]) '([1 2] [3 4] [1 2]))"]})

    (insert! :problems
             {:_id 31
              :title "Pack a Sequence"
              :times-solved 0
              :description "Write a function which packs consecutive duplicates into sub-lists."
              :tags ["easy" "seqs"]
              :approved true
              :tests ["(= (__ [1 1 2 1 1 1 3 3]) '((1 1) (2) (1 1 1) (3 3)))"
                      "(= (__ [:a :a :b :b :c]) '((:a :a) (:b :b) (:c)))"
                      "(= (__ [[1 2] [1 2] [3 4]]) '(([1 2] [1 2]) ([3 4])))"]})

    (insert! :problems
             {:_id 32
              :title "Duplicate a Sequence"
              :times-solved 0
              :description "Write a function which duplicates each element of a sequence."
              :tags ["easy" "seqs"]
              :approved true
              :tests ["(= (__ [1 2 3]) '(1 1 2 2 3 3))"
                      "(= (__ [:a :a :b :b]) '(:a :a :a :a :b :b :b :b))"
                      "(= (__ [[1 2] [3 4]]) '([1 2] [1 2] [3 4] [3 4]))"
                      "(= (__ [44 33]) [44 44 33 33])"]})

    (insert! :problems
             {:_id 33
              :title "Replicate a Sequence"
              :times-solved 0
              :description "Write a function which replicates each element of a sequence a variable number of times."
              :tags ["easy" "seqs"]
              :approved true
              :tests ["(= (__ [1 2 3] 2) '(1 1 2 2 3 3))"
                      "(= (__ [:a :b] 4) '(:a :a :a :a :b :b :b :b))"
                      "(= (__ [4 5 6] 1) '(4 5 6))"
                      "(= (__ [[1 2] [3 4]] 2) '([1 2] [1 2] [3 4] [3 4]))"
                      "(= (__ [44 33] 2) [44 44 33 33])"]})

    (insert! :problems
             {:_id 34
              :title "Implement range"
              :times-solved 0
              :restricted ["range"]
              :description "Write a function which creates a list of all integers in a given range."
              :tags ["easy" "seqs" "core-functions"]
              :approved true
              :tests ["(= (__ 1 4) '(1 2 3))"
                      "(= (__ -2 2) '(-2 -1 0 1))"
                      "(= (__ 5 8) '(5 6 7))"]})

    (insert! :problems
           {:_id 35
            :title "Local bindings"
            :times-solved 0
            :description "Clojure lets you give local names to values using the special let-form."
            :tags ["elementary" "syntax"]
            :approved true
            :tests ["(= __ (let [x 5] (+ 2 x)))"
                    "(= __ (let [x 3, y 10] (- y x)))"
                    "(= __ (let [x 21] (let [y 3] (/ x y))))"]})

  (insert! :problems
           {:_id 36
            :title "Let it Be"
            :times-solved 0
            :description "Can you bind x, y, and z so that these are all true?"
            :tags ["elementary" "math" "syntax"]
            :approved true
            :tests ["(= 10 (let __ (+ x y)))"
                    "(= 4 (let __ (+ y z)))"
                    "(= 1 (let __ z))"]})

    (insert! :problems
           {:_id 37
            :title "Regular Expressions"
            :times-solved 0
            :description "Regex patterns are supported with a special reader macro."
            :tags ["elementary" "regex" "syntax"]
            :approved true
            :tests ["(= __ (apply str (re-seq #\"[A-Z]+\" \"bA1B3Ce \")))"]})

    (insert! :problems
           {:_id 38
            :title "Maximum value"
            :times-solved 0
            :restricted ["max" "max-key"]
            :description "Write a function which takes a variable number of parameters and returns the maximum value."
            :tags ["easy" "core-functions"]
            :approved true
            :tests ["(= (__ 1 8 3 4) 8)"
                    "(= (__ 30 20) 30)"
                    "(= (__ 45 67 11) 67)"]})


    (insert! :problems
           {:_id 39
            :title "Interleave Two Seqs"
            :times-solved 0
            :restricted ["interleave"]
            :description "Write a function which takes two sequences and returns the first item from each, then the second item from each, then the third, etc."
            :tags ["easy" "seqs" "core-functions"]
            :approved true
            :tests ["(= (__ [1 2 3] [:a :b :c]) '(1 :a 2 :b 3 :c))"
                    "(= (__ [1 2] [3 4 5 6]) '(1 3 2 4))"
                    "(= (__ [1 2 3 4] [5]) [1 5])"
                    "(= (__ [30 20] [25 15]) [30 25 20 15])"]})



    (insert! :problems
           {:_id 40
            :title "Interpose a Seq"
            :times-solved 0
            :restricted ["interpose"]
            :description "Write a function which separates the items of a sequence by an arbitrary value."
            :tags ["easy" "seqs" "core-functions"]
            :approved true
            :tests ["(= (__ 0 [1 2 3]) [1 0 2 0 3])"
                    "(= (apply str (__ \", \" [\"one\" \"two\" \"three\"])) \"one, two, three\")"
                    "(= (__ :z [:a :b :c :d]) [:a :z :b :z :c :z :d])"]})

    (insert! :problems
           {:_id 41
            :title "Drop Every Nth Item"
            :times-solved 0
            :description "Write a function which drops every Nth item from a sequence."
            :tags ["easy" "seqs"]
            :approved true
            :tests ["(= (__ [1 2 3 4 5 6 7 8] 3) [1 2 4 5 7 8])"
                    "(= (__ [:a :b :c :d :e :f] 2) [:a :c :e])"
                    "(= (__ [1 2 3 4 5 6] 4) [1 2 3 5 6])"]})

    (insert! :problems
           {:_id 42
            :title "Factorial Fun"
            :times-solved 0
            :description "Write a function which calculates factorials."
            :tags ["easy" "math"]
            :approved true
            :tests ["(= (__ 1) 1)"
                    "(= (__ 3) 6)"
                    "(= (__ 5) 120)"
                    "(= (__ 8) 40320)"]})

    (insert! :problems
           {:_id 43
            :title "Reverse Interleave"
            :times-solved 0
            :description "Write a function which reverses the interleave process into x number of subsequences."
            :tags ["medium" "seqs"]
            :approved true
            :tests ["(= (__ [1 2 3 4 5 6] 2) '((1 3 5) (2 4 6)))"
                    "(= (__ (range 9) 3) '((0 3 6) (1 4 7) (2 5 8)))"
                    "(= (__ (range 10) 5) '((0 5) (1 6) (2 7) (3 8) (4 9)))"]})

    (insert! :problems
           {:_id 44
            :title "Rotate Sequence"
            :times-solved 0
            :description "Write a function which can rotate a sequence in either direction."
            :tags ["medium" "seqs"]
            :approved true
            :tests ["(= (__ 2 [1 2 3 4 5]) '(3 4 5 1 2))"
                    "(= (__ -2 [1 2 3 4 5]) '(4 5 1 2 3))"
                    "(= (__ 6 [1 2 3 4 5]) '(2 3 4 5 1))"
                    "(= (__ 1 '(:a :b :c)) '(:b :c :a))"
                    "(= (__ -4 '(:a :b :c)) '(:c :a :b))"]})

    (insert! :problems
             {:_id 45
              :title "Intro to Iterate"
              :times-solved 0
              :description "The iterate function can be used to produce an infinite lazy sequence."
              :tags ["easy" "seqs"]
              :approved true
              :tests ["(= __ (take 5 (iterate #(+ 3 %) 1)))"]})

    (insert! :problems
             {:_id 46
              :title "Flipping out"
              :times-solved 0
              :description "Write a higher-order function which flips the order of the arguments of an input function."
              :tags ["medium" "higher-order-functions"]
              :approved true
              :tests ["(= 3 ((__ nth) 2 [1 2 3 4 5]))"
                      "(= true ((__ >) 7 8))"
                      "(= 4 ((__ quot) 2 8))"
                      "(= [1 2 3] ((__ take) [1 2 3 4 5] 3))"]})

       (insert! :problems
           {:_id 47
            :title "Contain Yourself"
            :times-solved 0
            :description "The contains? function checks if a KEY is present in a given collection.  This often leads beginner clojurians to use it incorrectly with numerically indexed collections like vectors and lists."
            :tags ["easy"]
            :approved true
            :tests ["(contains? #{4 5 6} __)"
                    "(contains? [1 1 1 1 1] __)"
                    "(contains? {4 :a 2 :b} __)"
                    "(not (contains? '(1 2 4) __))"]})

       (insert! :problems
           {:_id 48
            :title "Intro to some"
            :times-solved 0
            :description "The some function takes a predicate function and a collection.  It returns the first logical true value of (predicate x) where x is an item in the collection."
            :tags ["easy"]
            :approved true
            :tests ["(= __ (some #{2 7 6} [5 6 7 8]))"
                    "(= __ (some #(when (even? %) %) [5 6 7 8]))"]})

       (insert! :problems
           {:_id 49
            :title "Split a sequence"
            :times-solved 0
            :restricted ["split-at"]
            :description "Write a function which will split a sequence into two parts."
            :tags ["easy" "seqs" "core-functions"]
            :approved true
            :tests ["(= (__ 3 [1 2 3 4 5 6]) [[1 2 3] [4 5 6]])"
                    "(= (__ 1 [:a :b :c :d]) [[:a] [:b :c :d]])"
                    "(= (__ 2 [[1 2] [3 4] [5 6]]) [[[1 2] [3 4]] [[5 6]]])"]})

       (insert! :problems
           {:_id 50
            :title "Split by Type"
            :times-solved 0
            :description "Write a function which takes a sequence consisting of items with different types and splits them up into a set of homogeneous sub-sequences. The internal order of each sub-sequence should be maintained, but the sub-sequences themselves can be returned in any order (this is why 'set' is used in the test cases)."
            :tags ["medium" "seqs"]
            :approved true
            :tests ["(= (set (__ [1 :a 2 :b 3 :c])) #{[1 2 3] [:a :b :c]})"
                    "(= (set (__ [:a \"foo\"  \"bar\" :b])) #{[:a :b] [\"foo\" \"bar\"]})"
                    "(= (set (__ [[1 2] :a [3 4] 5 6 :b])) #{[[1 2] [3 4]] [:a :b] [5 6]})"]})

       (insert! :problems
           {:_id 51
            :title "Advanced Destructuring"
            :times-solved 0
            :description "Here is an example of some more sophisticated destructuring."
            :tags ["easy" "destructuring"]
            :approved true
            :tests ["(= [1 2 [3 4 5] [1 2 3 4 5]] (let [[a b & c :as d] __] [a b c d]))"]})

      (insert! :problems
           {:_id 52
            :title "Intro to Destructuring"
            :times-solved 0
            :description "Let bindings and function parameter lists support destructuring."
            :tags ["easy" "destructuring"]
            :approved true
            :tests ["(= [2 4] (let [[a b c d e f g] (range)] __))"]})

      (insert! :problems
           {:_id 53
            :title "Longest Increasing Sub-Seq"
            :times-solved 0
            :description "Given a vector of integers, find the longest consecutive sub-sequence of increasing numbers. If two sub-sequences have the same length, use the one that occurs first. An increasing sub-sequence must have a length of 2 or greater to qualify."
            :tags ["hard" "seqs"]
            :approved true
            :tests ["(= (__ [1 0 1 2 3 0 4 5]) [0 1 2 3])"
		    "(= (__ [5 6 1 3 2 7]) [5 6])"
		    "(= (__ [2 3 3 4 5]) [3 4 5])"
		    "(= (__ [7 6 5 4]) [])"]})

      (insert! :problems
           {:_id 54
            :title "Partition a Sequence"
            :times-solved 0
            :restricted ["partition" "partition-all"]
            :description "Write a function which returns a sequence of lists of x items each.  Lists of less than x items should not be returned."
            :tags ["medium" "seqs" "core-functions"]
            :approved true
            :tests ["(= (__ 3 (range 9)) '((0 1 2) (3 4 5) (6 7 8)))"
		    "(= (__ 2 (range 8)) '((0 1) (2 3) (4 5) (6 7)))"
		    "(= (__ 3 (range 8)) '((0 1 2) (3 4 5)))"]})

      (insert! :problems
           {:_id 55
            :title "Count Occurences"
            :times-solved 0
            :restricted ["frequencies"]
            :description "Write a function which returns a map containing the number of occurences of each distinct item in a sequence."
            :tags ["medium" "seqs" "core-functions"]
            :approved true
            :tests ["(= (__ [1 1 2 3 2 1 1]) {1 4, 2 2, 3 1})"
                    "(= (__ [:b :a :b :a :b]) {:a 2, :b 3})"
                    "(= (__ '([1 2] [1 3] [1 3])) {[1 2] 1, [1 3] 2})"]})

      (insert! :problems
           {:_id 56
            :title "Find Distinct Items"
            :times-solved 0
            :restricted ["distinct"]
            :description "Write a function which removes the duplicates from a sequence. Order of the items must be maintained."
            :tags ["medium" "seqs" "core-functions"]
            :approved true
            :tests ["(= (__ [1 2 1 3 1 2 4]) [1 2 3 4])"
                    "(= (__ [:a :a :b :b :c :c]) [:a :b :c])"
                    "(= (__ '([2 4] [1 2] [1 3] [1 3])) '([2 4] [1 2] [1 3]))"]})

      (insert! :problems
           {:_id 56
            :title "Find Distinct Items"
            :times-solved 0
            :restricted ["distinct"]
            :description "Write a function which removes the duplicates from a sequence. Order of the items must be maintained."
            :tags ["medium" "seqs" "core-functions"]
            :approved true
            :tests ["(= (__ [1 2 1 3 1 2 4]) [1 2 3 4])"
                    "(= (__ [:a :a :b :b :c :c]) [:a :b :c])"
                    "(= (__ '([2 4] [1 2] [1 3] [1 3])) '([2 4] [1 2] [1 3]))"
                    "(= (__ (range 50)) (range 50))"]})

      (insert! :problems
           {:_id 57
            :title "Simple Recursion"
            :times-solved 0
            :description "A recursive function is a function which calls itself.  This is one of the fundamental techniques used in functional programming."
            :tags ["elementary" "recursion"]
            :approved true
            :tests ["(= __ ((fn foo [x] (when (> x 0) (conj (foo (dec x)) x))) 5))"]})

      (insert! :problems
           {:_id 58
            :title "Function Composition"
            :times-solved 0
            :restricted ["comp"]
            :description "Write a function which allows you to create function compositions.  The parameter list should take a variable number of functions, and create a function applies them from right-to-left."
            :tags ["medium" "higher-order-functions" "core-functions"]
            :approved true
            :tests ["(= [3 2 1] ((__ rest reverse) [1 2 3 4]))"
                    "(= 5 ((__ (partial + 3) second) [1 2 3 4]))"
                    "(= true ((__ zero? #(mod % 8) +) 3 5 7 9))"
                    "(= \"HELLO\" ((__ #(.toUpperCase %) #(apply str %) take) 5 \"hello world\"))"]})

      (insert! :problems
           {:_id 59
            :title "Juxtaposition"
            :times-solved 0
            :restricted ["juxt"]
            :description "Take a set of functions and return a new function that takes a variable number of arguments and returns a sequence containing the result of applying each function left-to-right to the argument list."
            :tags ["medium" "higher-order-functions" "core-functions"]
            :approved true
            :tests ["(= [21 6 1] ((__ + max min) 2 3 5 1 6 4))"
                    "(= [\"HELLO\" 5] ((__ #(.toUpperCase %) count) \"hello\"))"
                    "(= [2 6 4] ((__ :a :c :b) {:a 2, :b 4, :c 6, :d 8 :e 10}))"]})

      (insert! :problems
           {:_id 60
            :title "Sequence Reductions"
            :times-solved 0
            :restricted ["reductions"]
            :description "Write a function which behaves like reduce, but returns each intermediate value of the reduction.  Your function must accept either two or three arguments, and the return sequence must be lazy."
            :tags ["medium" "seqs" "core-functions"]
            :approved true
            :tests ["(= (take 5 (__ + (range))) [0 1 3 6 10])"
                    "(= (__ conj [1] [2 3 4]) [[1] [1 2] [1 2 3] [1 2 3 4]])"
                    "(= (last (__ * 2 [3 4 5])) (reduce * 2 [3 4 5]) 120)"]})

      (insert! :problems
           {:_id 61
            :title "Map Construction"
            :times-solved 0
            :restricted ["zipmap"]
            :description "Write a function which takes a vector of keys and a vector of values and constructs a map from them."
            :tags ["easy" "core-functions"]
            :approved true
            :tests ["(= (__ [:a :b :c] [1 2 3]) {:a 1, :b 2, :c 3})"
                    "(= (__ [1 2 3 4] [\"one\" \"two\" \"three\"]) {1 \"one\", 2 \"two\", 3 \"three\"})"
                    "(= (__ [:foo :bar] [\"foo\" \"bar\" \"baz\"]) {:foo \"foo\", :bar \"bar\"})"]})

      (insert! :problems
           {:_id 62
            :title "Re-implement Iteration"
            :times-solved 0
            :restricted ["iterate"]
            :description "Given a side-effect free function f and an initial value x write a function which returns an infinite lazy sequence of x, (f x), (f (f x)), (f (f (f x))), etc."
            :tags ["easy" "seqs" "core-functions"]
            :approved true
            :tests ["(= (take 5 (__ #(* 2 %) 1)) [1 2 4 8 16])"
                    "(= (take 100 (__ inc 0)) (take 100 (range)))"
                    "(= (take 9 (__ #(inc (mod % 3)) 1)) (take 9 (cycle [1 2 3])))"]})

      (insert! :problems
           {:_id 63
            :title "Group a Sequence"
            :times-solved 0
            :restricted ["group-by"]
            :description "Given a function f and a sequence s, write a function which returns a map.  The keys should be the values of f applied to each item in s.  The value at each key should be a vector of corresponding items in the order they appear in s."
            :tags ["medium" "seqs" "core-functions"]
            :approved true
            :tests ["(= (__ #(> % 5) #{1 3 6 8}) {false [1 3], true [6 8]})"
                    "(= (__ #(apply / %) [[1 2] [2 4] [4 6] [3 6]])\n   {1/2 [[1 2] [2 4] [3 6]], 2/3 [[4 6]]})"
                    "(= (__ count [[1] [1 2] [3] [1 2 3] [2 3]])\n   {1 [[1] [3]], 2 [[1 2] [2 3]], 3 [[1 2 3]]})"]})

      (insert! :problems
           {:_id 64
            :title "Intro to Reduce"
            :times-solved 0
            :description "<a href='http://clojuredocs.org/clojure_core/clojure.core/reduce'>Reduce</a> takes a 2 argument function and an optional starting value. It then applies the function to the first 2 items in the sequence (or the starting value and the first element of the sequence). In the next iteration the function will be called on the previous return value and the next item from the sequence, thus reducing the entire collection to one value. Don't worry, it's not as complicated as it sounds."
            :tags ["elementary" "seqs"]
            :approved true
            :tests ["(= 15 (reduce __ [1 2 3 4 5]))"
                    "(=  0 (reduce __ []))"
                    "(=  6 (reduce __ 1 [2 3]))"]})

      (insert! :problems
           {:_id 65
            :title "Black Box Testing"
            :times-solved 0
            :description "Clojure has many collection types, which act in subtly different ways. The core functions typically convert them into a uniform \"sequence\" type and work with them that way, but it can be important to understand the behavioral and performance differences so that you know which kind is appropriate for your application.<br /><br />Write a function which takes a collection and returns one of :map, :set, :list, or :vector - describing the type of collection it was given.<br />You won't be allowed to inspect their class or use the built-in predicates like list? - the point is to poke at them and understand their behavior."
            :tags ["hard" "seqs" "testing"]
            :approved true
            :tests ["(= :map (__ {:a 1, :b 2}))",
                    "(= :list (__ (range (rand-int 20))))",
                    "(= :vector (__ [1 2 3 4 5 6]))",
                    "(= :set (__ #{10 (rand-int 5)}))",
                    "(= [:map :set :vector :list] (map __ [{} #{} [] ()]))"]
            :restricted (map str '[class type Class vector? sequential?
                                   list? seq? map? set? instance? getClass])})

      (insert! :problems
          {:_id 66
           :title "Greatest Common Divisor"
           :times-solved 0
           :description "Given two integers, write a function which
returns the greatest common divisor."
           :tags ["easy"]
           :approved true
           :tests ["(= (__ 2 4) 2)"
                   "(= (__ 10 5) 5)"
                   "(= (__ 5 7) 1)"
                   "(= (__ 1023 858) 33)"]})

      (insert! :problems
          {:_id 67
           :title "Prime Numbers"
           :times-solved 0
           :description "Write a function which returns the first x
number of prime numbers."
           :tags ["medium" "primes"]
           :approved true
           :tests ["(= (__ 2) [2 3])"
                   "(= (__ 5) [2 3 5 7 11])"
                   "(= (last (__ 100)) 541)"]})

      (insert! :problems
          {:_id 68
           :title "Recurring Theme"
           :times-solved 0
           :description "Clojure only has one non-stack-consuming looping construct: recur.  Either a function or a loop can be used as the recursion point.  Either way, recur rebinds the bindings of the recursion point to the values it is passed.  Recur must be called from the tail-position, and calling it elsewhere will result in an error."
           :tags ["elementary" "recursion"]
           :approved true
           :tests ["(= __\n  (loop [x 5\n         result []]\n    (if (> x 0)\n      (recur (dec x) (conj result (+ 2 x)))\n      result)))"]})

      (insert! :problems
          {:_id 69
           :title "Merge with a Function"
           :times-solved 0
           :restricted ["merge-with"]
           :description "Write a function which takes a function f and a variable number of maps.  Your function should return a map that consists of the rest of the maps conj-ed onto the first.  If a key occurs in more than one map, the mapping(s) from the latter (left-to-right) should be combined with the mapping in the result by calling (f val-in-result val-in-latter)"
           :tags ["medium" "core-functions"]
           :approved true
           :tests ["(= (__ * {:a 2, :b 3, :c 4} {:a 2} {:b 2} {:c 5})\n   {:a 4, :b 6, :c 20})"
                   "(= (__ - {1 10, 2 20} {1 3, 2 10, 3 15})\n   {1 7, 2 10, 3 15})"
                   "(= (__ concat {:a [3], :b [6]} {:a [4 5], :c [8 9]} {:b [7]})\n   {:a [3 4 5], :b [6 7], :c [8 9]})"]})

      (insert! :problems
          {:_id 70
           :title "Word Sorting"
           :times-solved 0
           :description "Write a function which splits a sentence up into a sorted list of words.  Capitalization should not affect sort order and punctuation should be ignored."
           :tags ["medium" "sorting"]
           :approved true
           :tests ["(= (__  \"Have a nice day.\")\n   [\"a\" \"day\" \"Have\" \"nice\"])"
                   "(= (__  \"Clojure is a fun language!\")\n   [\"a\" \"Clojure\" \"fun\" \"is\" \"language\"])"
                   "(= (__  \"Fools fall for foolish follies.\")\n   [\"fall\" \"follies\" \"foolish\" \"Fools\" \"for\"])"]})

      (insert! :problems
          {:_id 71
           :title "Rearranging Code: ->"
           :times-solved 0
           :description "The -> macro threads an expression x through a variable number of forms. First, x is inserted as the second item in the first form, making a list of it if it is not a list already.  Then the first form is inserted as the second item in the second form, making a list of that form if necessary.  This process continues for all the forms.  Using -> can sometimes make your code more readable."
           :tags ["elementary"]
           :approved true
           :tests ["(= (__ (sort (rest (reverse [2 5 4 1 3 6]))))\n   (-> [2 5 4 1 3 6] reverse rest sort __)\n   5)"]})

      (insert! :problems
          {:_id 72
           :title "Rearranging Code: ->>"
           :times-solved 0
           :description "The ->> macro threads an expression x through a variable number of forms. First, x is inserted as the last item in the first form, making a list of it if it is not a list already.  Then the first form is inserted as the last item in the second form, making a list of that form if necessary.  This process continues for all the forms.  Using ->> can sometimes make your code more readable."
           :tags ["elementary"]
           :approved true
           :tests ["(= (__ (map inc (take 3 (drop 2 [2 5 4 1 3 6]))))\n   (->> [2 5 4 1 3 6] (drop 2) (take 3) (map inc) (__))\n   11)"]})

      (insert! :problems
          {:_id 73
           :title "Analyze a Tic-Tac-Toe Board"
           :times-solved 0
           :description "A <a href=\"http://en.wikipedia.org/wiki/Tic-tac-toe\">tic-tac-toe</a> board is represented by a two dimensional vector. X is represented by :x, O is represented by :o, and empty is represented by :e.  A player wins by placing three Xs or three Os in a horizontal, vertical, or diagonal row.  Write a function which analyzes a tic-tac-toe board and returns :x if X has won, :o if O has won, and nil if neither player has won."
           :tags ["medium" "game"]
	   :approved true
           :tests ["(= nil (__ [[:e :e :e]\n            [:e :e :e]\n            [:e :e :e]]))"
		   "(= :x (__ [[:x :e :o]\n           [:x :e :e]\n           [:x :e :o]]))"
		   "(= :o (__ [[:e :x :e]\n           [:o :o :o]\n           [:x :e :x]]))"
		   "(= nil (__ [[:x :e :o]\n            [:x :x :e]\n            [:o :x :o]]))"
		   "(= :x (__ [[:x :e :e]\n           [:o :x :e]\n           [:o :e :x]]))"
		   "(= :o (__ [[:x :e :o]\n           [:x :o :e]\n           [:o :e :x]]))"
		   "(= nil (__ [[:x :o :x]\n            [:x :o :x]\n            [:o :x :o]]))"]}) 

      (insert! :problems
          {:_id 74
           :title "Filter Perfect Squares"
           :times-solved 0
           :description "Given a string of comma separated integers, write a function which returns a new comma separated string that only contains the numbers which are perfect squares."
           :tags ["medium"]
	   :approved true
           :tests ["(= (__ \"4,5,6,7,8,9\") \"4,9\")"
		   "(= (__ \"15,16,25,36,37\") \"16,25,36\")"]}) 

      (insert! :problems
          {:_id 75
           :title "Euler's Totient Function"
           :times-solved 0
           :description "Two numbers are coprime if their greatest common divisor equals 1.  Euler's totient function f(x) is defined as the number of positive integers less than x which are coprime to x.  The special case f(1) equals 1.  Write a function which calculates Euler's totient function."
           :tags ["medium"]
	   :approved true
           :tests ["(= (__ 1) 1)"
		   "(= (__ 10) (count '(1 3 7 9)) 4)"
		   "(= (__ 40) 16)"
		   "(= (__ 99) 60)"]}) 

      (insert! :problems
          {:_id 76
           :title "Intro to Trampoline"
           :times-solved 0
           :description "The trampoline function takes a function f and a variable number of parameters.  Trampoline calls f with any parameters that were supplied.  If f returns a function, trampoline calls that function with no arguments.  This is repeated, until the return value is not a function, and then trampoline returns that non-function value.  This is useful for implementing mutually recursive algorithms in a way that won't consume the stack."
           :tags ["medium", "recursion"]
	   :approved true
           :tests ["(= __\n   (letfn\n     [(foo [x y] #(bar (conj x y) y))\n      (bar [x y] (if (> (last x) 10)\n                   x\n                   #(foo x (+ 2 y))))]\n     (trampoline foo [] 1)))"]})

      (insert! :problems
          {:_id 77
           :title "Anagram Finder"
           :times-solved 0
           :description "Write a function which finds all the anagrams in a vector of words.  A word x is an anagram of word y if all the letters in x can be rearranged in a different order to form y.  Your function should return a set of sets, where each sub-set is a group of words which are anagrams of each other.  Each sub-set should have at least two words.  Words without any anagrams should not be included in the result."
           :tags ["medium"]
	   :approved true
           :tests ["(= (__ [\"meat\" \"mat\" \"team\" \"mate\" \"eat\"])\n   #{#{\"meat\" \"team\" \"mate\"}})"
		   "(= (__ [\"veer\" \"lake\" \"item\" \"kale\" \"mite\" \"ever\"])\n   #{#{\"veer\" \"ever\"} #{\"lake\" \"kale\"} #{\"mite\" \"item\"}})"]})
      
      (insert! :problems
           {:_id 78
            :title "Reimplement Trampoline"
            :times-solved 0
            :restricted ["trampoline"]
            :description "Reimplement the function described in <a href=\"76\"> \"Intro to Trampoline\"</a>."
            :approved true
            :tags ["medium" "core-functions"]
            :tests ["(= (letfn [(triple [x] #(sub-two (* 3 x)))\n          (sub-two [x] #(stop?(- x 2)))\n          (stop? [x] (if (> x 50) x #(triple x)))]\n    (__ triple 2))\n  82)"
                    "(= (letfn [(my-even? [x] (if (zero? x) true #(my-odd? (dec x))))\n          (my-odd? [x] (if (zero? x) false #(my-even? (dec x))))]\n    (map (partial __ my-even?) (range 6)))\n  [true false true false true false])"]})

      (insert! :problems
           {:_id 79
            :title "Triangle Minimal Path"
            :times-solved 0
            :description "Write a function which calculates the sum of the minimal path through a triangle.  The triangle is represented as a vector of vectors.  The path should start at the top of the triangle and move to an adjacent number on the next row until the bottom of the triangle is reached."
            :approved true
            :tags ["hard"]
            :tests ["(= (__ [   [1]\n          [2 4]\n         [5 1 4]\n        [2 3 4 5]])\n   (+ 1 2 1 3)\n   7)"
		    "(= (__ [     [3]\n            [2 4]\n           [1 9 3]\n          [9 9 2 4]\n         [4 6 6 7 8]\n        [5 7 3 5 1 4]])\n   (+ 3 4 3 2 7 1)\n   20)"]})

      (insert! :problems
           {:_id 80
            :title "Perfect Numbers"
            :times-solved 0
            :description "A number is \"perfect\" if the sum of its divisors equal the number itself.  6 is a perfect number because 1+2+3=6.  Write a function which returns true for perfect numbers and false otherwise."
            :approved true
            :tags ["medium"]
            :tests ["(= (__ 6) true)"
		    "(= (__ 7) false)"
		    "(= (__ 496) true)"
		    "(= (__ 500) false)"
		    "(= (__ 8128) true)"]})

(insert! :problems
           {:_id 81
            :title "Set Intersection"
            :times-solved 0
            :restricted ["intersection"]
            :description "Write a function which returns the intersection of two sets.  The intersection is the sub-set of items that each set has in common."
            :approved true
            :tags ["easy" "set-theory"]
            :tests ["(= (__ #{0 1 2 3} #{2 3 4 5}) #{2 3})"
		    "(= (__ #{0 1 2} #{3 4 5}) #{})"
		    "(= (__ #{:a :b :c :d} #{:c :e :a :f :d}) #{:a :c :d})"]})

(insert! :problems
           {:_id 82
            :title "Word Chains"
            :times-solved 0
            :description "A word chain consists of a set of words ordered so that each word differs by only one letter from the words directly before and after it.  The one letter difference can be either an insertion, a deletion, or a substitution.  Here is an example word chain:<br/><br/>cat -> cot -> coat -> oat -> hat -> hot -> hog -> dog<br/><br/>Write a function which takes a sequence of words, and returns true if they can be arranged into one continous word chain, and false if they cannot."
            :approved true
            :tags ["hard" "seqs"]
            :tests ["(= true (__ #{\"hat\" \"coat\" \"dog\" \"cat\" \"oat\" \"cot\" \"hot\" \"hog\"}))"
                    "(= false (__ #{\"cot\" \"hot\" \"bat\" \"fat\"}))"
                    "(= false (__ #{\"to\" \"top\" \"stop\" \"tops\" \"toss\"}))"
                    "(= true (__ #{\"spout\" \"do\" \"pot\" \"pout\" \"spot\" \"dot\"}))"
                    "(= true (__ #{\"share\" \"hares\" \"shares\" \"hare\" \"are\"}))"
                    "(= false (__ #{\"share\" \"hares\" \"hare\" \"are\"}))"]})

(insert! :problems
           {:_id 83
            :title "A Half-Truth"
            :times-solved 0
            :description "Write a function which takes a variable number of booleans.  Your function should return true if some of the parameters are true, but not all of the parameters are true.  Otherwise your function should return false."
            :approved true
            :tags ["easy"]
            :tests ["(= false (__ false false))"
                    "(= true (__ true false))"
                    "(= false (__ true))"
                    "(= true (__ false true false))"
                    "(= false (__ true true true))"
                    "(= true (__ true true true false))"]})

(insert! :problems
           {:_id 84
            :title "Transitive Closure"
            :times-solved 0
            :description "Write a function which generates the <a href=\"http://en.wikipedia.org/wiki/Transitive_closure\">transitive closure</a> of a <a href=\"http://en.wikipedia.org/wiki/Binary_relation\">binary relation</a>.  The relation will be represented as a set of 2 item vectors."
            :approved true
            :tags ["hard" "set-theory"]
            :tests ["(let [divides #{[8 4] [9 3] [4 2] [27 9]}]\n  (= (__ divides) #{[4 2] [8 4] [8 2] [9 3] [27 9] [27 3]}))"
                    "(let [more-legs\n      #{[\"cat\" \"man\"] [\"man\" \"snake\"] [\"spider\" \"cat\"]}]\n  (= (__ more-legs)\n     #{[\"cat\" \"man\"] [\"cat\" \"snake\"] [\"man\" \"snake\"]\n       [\"spider\" \"cat\"] [\"spider\" \"man\"] [\"spider\" \"snake\"]}))"
                    "(let [progeny\n      #{[\"father\" \"son\"] [\"uncle\" \"cousin\"] [\"son\" \"grandson\"]}]\n  (= (__ progeny)\n     #{[\"father\" \"son\"] [\"father\" \"grandson\"]\n       [\"uncle\" \"cousin\"] [\"son\" \"grandson\"]}))"]})

(insert! :problems
          {:_id 85
           :title "Power Set"
           :times-solved 0
           :description "Write a function which generates the <a href=\"http://en.wikipedia.org/wiki/Power_set\">power set</a> of a given set.  The power set of a set x is the set of all subsets of x, including the empty set and x itself."
           :approved true
           :tags ["hard" "set-theory"]
           :tests ["(= (__ #{1 :a}) #{#{1 :a} #{:a} #{} #{1}})"
                   "(= (__ #{}) #{#{}})"
                   "(= (__ #{1 2 3})\n   #{#{} #{1} #{2} #{3} #{1 2} #{1 3} #{2 3} #{1 2 3}})"
                   "(= (count (__ (into #{} (range 10)))) 1024)"]})

(insert! :problems
          {:_id 86
           :title "Happy numbers"
           :times-solved 0
           :description "Happy numbers are positive integers that follow a particular formula: take each individual digit, square it, and then sum the squares to get a new number. Repeat with the new number and eventually, you might get to a number whose squared sum is 1. This is a happy number. An unhappy number (or sad number) is one that loops endlessly. Write a function that determines if a number is happy or not."
           :tags ["easy" "math"]
           :approved true
           :tests ["(= (__ 7) true)"
                   "(= (__ 986543210) true)"
                   "(= (__ 2) false)"
                   "(= (__ 3) false)"]})

      (insert! :problems
          {:_id 87
           :title "Create an Equation"
           :times-solved 0
           :description "Write a function which takes three or more integers.  Using these integers, your function should generate clojure code representing an equation.  The following rules for the equation must be satisfied:\n\n    1. All integers must be used once and only once.\n    2. The order of the integers must be maintained when reading the equation left-to-right.\n    3. The only functions you may use are +, *, or =.\n    4. The equation must use the minimum number of parentheses.\n    5. If no satisfying equation exists, return nil."
           :tags ["hard", "code-generation"]
	   :approved true
           :tests ["(= (__ 3 4 7) '(= (+ 3 4) 7))"
		   "(= (__ 3 4 12) '(= (* 3 4) 12))"
		   "(= (__ 3 4 14) nil)"
		   "(= (__ 3 4 5 35) '(= (* (+ 3 4) 5) 35))"
		   "(= (__ 3 4 5 60) '(= (+ (* 3 4) 5) 60))"
		   "(= (__ 3 4 5 23) '(= (+ 3 (* 4 5)) 23))"
		   "(= (__ 3 4 5 27) '(= (* 3 (+ 4 5)) 27))"
		   "(= (__ 3 4 5 6) nil)"
		   "(= (__ 1 2 10 100 2001) '(= (+ 1 (* 2 10 100)) 2001)"
		   "(= (__ 1 2 10 100 1300) '(= (* (+ 1 2 10) 100) 1300)"]})
      
      ))
