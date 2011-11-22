(ns foreclojure.messages)

(defn load-props [file] 
  (into {} (doto (java.util.Properties.)
             (.load (-> (Thread/currentThread)
             (.getContextClassLoader)
             (.getResourceAsStream file))))))
             
(def err-msgs (load-props "error-messages.properties"))