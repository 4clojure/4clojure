(ns foreclojure.ring
  (:require [clojure.java.io           :as   io]
            [clojure.string            :as   s]
            [compojure.route           :as   route]
            [cheshire.core             :as   json])
  (:import  [java.net                  URL])
  (:use     [compojure.core            :only [GET routes]]
            [hiccup.core               :only [html]]
            [foreclojure.version-utils :only [strip-version-number]]
            [foreclojure.ring-utils    :only [get-host]]
            [useful.debug              :only [?]]
            [ring.util.response        :only [response]]))

;; copied from compojure.route, modified to use File instead of Stream
(defn resources
  "A route for serving resources on the classpath. Accepts the following
  keys:
    :root - the root prefix to get the resources from. Defaults to 'public'."
  [path & [options]]
  (GET path {{resource-path :*} :route-params}
    (let [root (:root options "public")]
      (when-let [res (io/resource (str root "/" resource-path))]
        (response (io/as-file res))))))

(defn wrap-url-as-file [handler]
  (fn [request]
    (when-let [{body :body :as resp} (handler request)]
      (if (and (instance? URL body)
               (= "file" (.getProtocol ^URL body)))
        (update-in resp [:body] io/as-file)
        resp))))

(defn wrap-strip-trailing-slash [handler]
  (fn [request]
    (handler (update-in request [:uri] s/replace #"(?<=.)/$" ""))))

(defn wrap-versioned-expiry [handler]
  (fn [request]
    (when-let [resp (handler
                     (update-in request [:uri] strip-version-number))]
      (assoc-in resp [:headers "Cache-control"]
                "public, max-age=31536000"))))

(defn wrap-debug [handler label]
  (fn [request]
    (println "In" label)
    (? (handler (? request)))))

(let [content-type [:headers "Content-Type"]]
  (defn wrap-json [handler]
    (fn [request]
      (when-let [resp (handler request)]
        (-> resp
            (assoc-in content-type "application/json")
            (update-in [:body] json/generate-string))))))

(defn split-hosts [host-handlers]
  (let [default (:default host-handlers)]
    (fn [request]
      (let [host (get-host request)
            handler (or (host-handlers host) default)]
        (handler request)))))

(def render-404
  (html
   [:head
    [:title "4clojure: Page not found"]]
   [:body
    [:div {:style "margin-left: auto; margin-right: auto; width: 300px;"}
     [:p {:style "text-align: center; width: 100%; margin-top: 45px; font-family: helvetica; color: gray; font-size: 25px;"} "404 &mdash; Page not found."]
     [:img {:style "margin-left: 18px;" :src "/images/4clj-gus-confused-small.png"}]]]))

(defn wrap-404 [handler]
  (routes handler
          (route/not-found render-404)))