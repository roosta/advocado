(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [advocado.config :refer [env]]
   [clojure.pprint]
   [clojure.spec.alpha :as s]
   [advocado.email :as email]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [advocado.figwheel :refer [start-fw stop-fw cljs]]
   [advocado.core :refer [start-app]]
   [advocado.db.core :as db]
   [buddy.hashers :as hashers]
   [conman.core :as conman]
   [luminus-migrations.core :as migrations]))

(defn uuid []
  (java.util.UUID/randomUUID))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'advocado.core/repl-server))

(defn stop
  "Stops application."
  []
  (mount/stop-except #'advocado.core/repl-server))

(defn restart
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db
  "Restarts database."
  []
  (mount/stop #'advocado.db.core/*db*)
  (mount/start #'advocado.db.core/*db*)
  (binding [*ns* 'advocado.db.core]
    (conman/bind-connection advocado.db.core/*db* "sql/queries.sql")))

(defn reset-db
  "Resets database."
  []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate
  "Migrates database up for all outstanding migrations."
  []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback
  "Rollback latest database migration."
  []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration
  "Create a new up and down migration file with a generated timestamp and `name`."
  [name]
  (migrations/create name (select-keys env [:database-url])))

