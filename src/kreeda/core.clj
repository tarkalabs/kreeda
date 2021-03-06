(ns kreeda.core 
  (:require [compojure.core :refer [defroutes GET ANY]]
            [compojure.route :as route]
            [noir.util.middleware :refer [app-handler]]
            [noir.response :as resp]
            [environ.core :refer [env]]
            [ring.middleware.session.cookie :as c]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.logger :as logger]
            [cemerick.friend :as friend]
            [kreeda.auth :as auth]
            [kreeda.controllers.applications :as applications]
            [kreeda.controllers.actions :as actions]
            [kreeda.controllers.sdk :as sdk]
            [kreeda.layout :as layout]))

(defroutes base-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defroutes app-routes
  (GET "/" [request] (layout/render "index.html" {:name "Clojure Cup"
                                                  :current-user (auth/current-user request)}))
  (GET "/auth/github" request (auth/authorize (resp/redirect  "/app")))
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
  (GET "/app" request (auth/authorize (layout/render "app.html" {:current_user (auth/current-user request)})))
  )

(defroutes api-routes
  (GET "/current_user" request 
       (auth/authorize (resp/json (auth/current-user request)))))

(def app (wrap-reload
           (logger/wrap-with-logger
             (app-handler [(auth/wrap-authentication app-routes)
                           sdk/sdk-routes
                           applications/app actions/app
                           (auth/wrap-authentication api-routes) base-routes] 
                          :session-options {:cookie-attrs {:max-age (* 60 60 24 365)} 
                                            :store (c/cookie-store {:key (env :cookie-secret)} )}
                          :formats [:json-kw]))))
