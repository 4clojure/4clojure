(ns foreclojure.feeds
  (:require [clojure.data.xml :refer [emit-str element]]))

(defn create-feed
  "Creates a feed with title, link, description, a link to the location
   of the feed itself, and is populated with a collection of items in the
   following format:

   (element :item
     (element :guid \"http://example.com/location/of/item/1\")
     (element :title \"Title of Item\")
     (element :description \"Description of Item\"))"
  [feed-title feed-link feed-description resource-link items]
  (emit-str
   (element :rss {:version "2.0"
                  :xmlns:atom "http://www.w3.org/2005/Atom"}
     (apply element :channel {}
       (element :atom:link {:href resource-link
                            :rel "self"
                            :type "application/rss+xml"})
       (element :title {} feed-title)
       (element :link {} feed-link)
       (element :descrption {} feed-description)
       items))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

