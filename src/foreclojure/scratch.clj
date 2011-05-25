;;; Wrote this while trying to roll my own version of Last-Modified header
;;; management. It turned out that Ring already has a middleware for this
;;; (I was looking in Compojure instead of Ring), so this code isn't
;;; necessary, but I'm disinclined to throw it out, since it's an interesting
;;; example of how to work with HTTP dates in Clojure

;; DateFormat isn't thread-safe, so don't globally def one
(defn rfc-date-formatter []
  (doto (SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss 'GMT'")
    (.setCalendar (-> "GMT"
                      (TimeZone/getTimeZone)
                      (Calendar/getInstance)))))

(defn format-date [date]
  (.format (rfc-date-formatter) date))

(defn parse-date [date-str]
  (.parse (rfc-date-formatter) date-str))

(defn wrap-modtime [handler]
  (fn [request]
    (when-let [{body :body :as resp} (handler request)]
      (if (instance? File body)
        (let [modtime (.lastModified ^File body)]
          (assoc-in resp [:headers "Last-Modified"]
                    (format-date (Date. modtime))))
        resp))))
