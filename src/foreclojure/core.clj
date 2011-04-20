(ns foreclojure.core
  (:use compojure.core
        [foreclojure static problems login register users]
        ring.adapter.jetty
        somnium.congomongo
        [ring.middleware.reload :only [wrap-reload]])
  (:require [compojure [route :as route] [handler :as handler]]
            [sandbar.stateful-session :as session]
            [ring.util.response :as response]))

(mongo!
 :db "mydb")
(add-index! :users [:user] :unique true)
(add-index! :users [[:solved -1]])

(defroutes main-routes
  (GET "/" [] (welcome-page))
  
  (GET  "/login" [] (my-login-page))
  (POST "/login" {{:strs [user pwd]} :form-params}
        (do-login user pwd))
  (GET "/logout" []
       (do (session/session-delete-key! :user)
           (response/redirect "/")))
  (GET  "/register" [] (register-page))
  (POST "/register" {{:strs [user pwd repeat-pwd email]} :form-params}
        (do-register user pwd repeat-pwd email))

  (GET "/problems" [] (problem-page))
  (GET "/problem/:id" [id] (code-box id))
  (POST "/run-code" {{:strs [id code]} :form-params}
        (run-code (Integer. id) code))

  (GET "/users" [] (users-page))
  (GET "/links" [] (links-page))
  (GET "/directions" [] (getting-started-page))

  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site
   (session/wrap-stateful-session
    (wrap-reload #'main-routes '(foreclojure.core)))))

(defn run []
  (run-jetty (var app) {:join? false :ssl? true :port 8080 :ssl-port 8443
                        :keystore "keystore"
                        :key-password "dev_pass"}))

(defn -main [& args]
  (run))
