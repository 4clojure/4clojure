(ns foreclojure.feeds
  (:use [clojure.contrib.prxml :only [prxml]]))

(defn escape [x]
  (str "<![CDATA[" x "]>"))

(defn create-feed
  "Creates a feed with title, link, description, a link to the location of the feed itself, and is populated with a collection of items in the following format:

  [:item [:guid \"http://example.com/location/of/item/1\"]
         [:title \"Title of Item\"]
         [:description \"Description of Item\"]]"
  [feed-title feed-link feed-description resource-link items]
  (with-out-str
    (prxml [:decl! {:version "1.0"}]
           [:rss {:version "2.0"
                  :xmlns:atom "http://www.w3.org/2005/Atom"}
            [:channel
             [:atom:link
              {:href resource-link
               :rel "self"
               :type "application/rss+xml"}]
             [:title feed-title]
             [:link feed-link]
             [:description (escape feed-description)]
             items]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Example Feed
(comment
  (defn problem-feed [n]
    (reduce (fn [feed v]
              (conj feed [:item
                          [:guid (str "http://4clojure.com/problem/" (:_id v))]
                          [:title (:title v)]
                          [:description (:description v)]]))
            () (get-recent-problems n))))


;; Testing Validity
;; Paste the resulting test.xml file into the w3c validator
;; to verify your feed is valid.
(comment
  (spit (java.io.File. "test.xml")
        (create-feed  "Recent Problems"
                      "http://4clojure.com"
                      "Recent problems at 4clojure"
                      "http://4clojure.com/problems/rss"
                      (problem-feed 20))))

