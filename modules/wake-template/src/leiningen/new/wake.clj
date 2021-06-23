(ns leiningen.new.wake
  (:require
    [leiningen.core.main :as main]
    [leiningen.new.templates
     :refer [renderer
             name-to-path
             ->files
             sanitize
             sanitize-ns
             project-name]]
    [leiningen.new.options.base :as base]
    [leiningen.new.options.helpers :as helpers]
    [clojure.set :as set]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Files & Data for Template

(defn app-files [data]
  (concat
    (base/files data)
    ;; TODO: SQL files add: queries.sql and migrations folder
    ;; If needed, can have conditional files here
    ))

(defn template-data [name options]
  (let [full? (helpers/option? "+full" options)]
    {:full-name name
     :name      (project-name name)
     :ns-name   (sanitize-ns name)
     :sanitized (name-to-path name)

     :crux?     (or full? (helpers/option? "+crux" options))
     :sql?      (or full? (helpers/option? "+sql" options))
     :hato?     (or full? (helpers/option? "+hato" options))
     :metrics?  (or full? (helpers/option? "+metrics" options))
     :quartz?   (or full? (helpers/option? "+quartz" options))
     :redis?    (or full? (helpers/option? "+redis" options))
     :selmer?   (or full? (helpers/option? "+selmer" options))

     :repl?     (not (helpers/option? "+bare" options))}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Check Options

(def available-set
  #{"+bare"
    "+full"
    "+crux"
    "+hato"
    "+quartz"
    "+redis"
    "+selmer"
    "+sql"})

(defn check-available
  [options]
  (let [options-set (into #{} options)
        abort?      (not (set/superset? available-set
                                        options-set))]
    (when abort?
      (main/abort "\nError: invalid profile(s)\n"))))

(defn check-conflicts
  [options]
  (when (> (count (filter #{"+full" "+bare"} options))
           1)
    (main/abort "\nCannot have both +full and +bare profile present\n")))

(defn check-options
  "Check the user-provided options"
  [options]
  (doto options
    (check-available)
    (check-conflicts)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main

(defn wake [name & options]
  (check-options options)
  (let [data (template-data name options)]
    (main/info "Generating wake project.")
    (apply ->files data (app-files data))))
