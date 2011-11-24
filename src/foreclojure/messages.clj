(ns foreclojure.messages)

(defn load-props [file] 
  (into {} (doto (java.util.Properties.)
             (.load (-> (Thread/currentThread)
             (.getContextClassLoader)
             (.getResourceAsStream file))))))
 
(def err-msg-map  (load-props "error-messages.properties"))             
             
(defn err-msg [key & args]
      (apply format (cons (get err-msg-map key) args)))