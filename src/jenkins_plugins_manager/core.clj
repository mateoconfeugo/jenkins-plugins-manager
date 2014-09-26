(ns jenkins-plugins-manager.core
  ^ {:author "Matthew Burns"
     :doc "Creates a report that contains the usage metrics of the various plugins in jenkins"}
    (:require [clojure.core :refer [re-find re-seq]]
              [clojure.java.io :as io]
              [clojure.string :as str]
              [clojure.pprint :refer [pprint]]
              [clojure.tools.cli :refer [cli]]
              [clojure.xml :as xml]
              [plumbing.core :refer :all]
              [plumbing.graph :as graph]
              [plumbing.fnk.pfnk :as pfnk]
              [shoreleave.server-helpers :refer [safe-read]])
    (:gen-class))

(defn read-config
  "Read a config file and return it as Clojure Data.  Usually, this is a hashmap"
  ([]
     (read-config (str (System/getProperty "user.dir") "/resources/config.edn")))
  ([config-loc]
     (safe-read (slurp config-loc))))

(def config (read-config))

(defn read-lines [filename]
  "Transform a filename into a lazy sequence of the lines in said file"
  (let [rdr (io/reader filename)]
    (defn read-next-line []
      (if-let [line (.readLine rdr)]
        (cons line (lazy-seq (read-next-line)))
        (.close rdr)))
    (lazy-seq (read-next-line))))

(defn regex-file-seq
  "Lazily filter a directory based on a regex."
  [re dir]
  (filter #(re-find re (.getPath %)) (file-seq dir)))

(defn get-plugins-present
  "From a file that contains a name and description of each plugin create a map of plugin name keywords and nil for value"
  [lines]
  (->> lines
       (map #(-> (re-seq  #"\((.*)\):" %) first (nth 1) keyword))
       (reduce (fn [c p] (assoc c p 0)) {})))

(defn job-plugins
  "Extract the plugins used in a job file into a vector"
  [file]
  (->> file
       read-lines
       (map #(-> (re-seq #"plugin=\"(.*)@" %) first (nth 1) keyword))
       (reduce (fn [c p] (into [] (remove nil? (conj c p)))) #{})))

(defn plugins-used
  "Parse job files creating a report of amount usage for each  plugin"
  [files plugins]
  (->> files
       (map #(job-plugins %))
       flatten
       (reduce (fn [c p] (update-in c [p] inc)) plugins)))

(defn  usage-report
  "Return a map of used and inactive plugins as well as the guts representing
   the steps in taking a set of plugins and finding the usage metrics across
   a set of jenkins jobs"
  [{:keys [xml-cfg-dir plugins-filepath]}]
  (let [jobs-dirpath (format "%s/%s/%s" (System/getProperty "user.dir") "resources" xml-cfg-dir)
        logic-graph  {:raw-plugin-lines (fnk [plugins-filepath] (read-lines plugins-filepath))
                      :job-files (fnk [jobs-dirpath] (regex-file-seq (re-pattern  ".*\\.xml") (clojure.java.io/file jobs-dirpath)))
                      :plugins-present (fnk [raw-plugin-lines] (get-plugins-present raw-plugin-lines))
                      :plugin-use  (fnk [job-files plugins-present](plugins-used job-files plugins-present))
                      :used (fnk [plugin-use] (filter (fn [k] (< 0 (k plugin-use))) (keys plugin-use)))
                      :unused (fnk [plugin-use] (filter (fn [k] (= 0 (k plugin-use))) (keys plugin-use)))}
        logic (graph/eager-compile logic-graph)]
    {:logic logic
     :logic-graph logic-graph
     :used (into [](:used (logic {:jobs-dirpath jobs-dirpath :plugins-filepath plugins-filepath})))
     :inactive (into [](:unused (logic {:jobs-dirpath jobs-dirpath :plugins-filepath plugins-filepath})))}))

(defn -main [& args]
  (let [[opts args banner] (cli args ["-h" "--help" "Print this help" :default false :flag true]
                                ["-c" "--configs" "directory that contains the configuration files"
                                 :default (:job-dump-dir config) :flag false]
                                ["-p" "--plugins" "file that contains all the plugins present files"
                                 :default (format "%s/%s/%s" (System/getProperty "user.dir") "resources" "all-jenkins-plugins.txt") :flag false])
        report (usage-report {:xml-cfg-dir (:configs opts)  :plugins-filepath (:plugins opts)})
        used (map name (:used report))
        inactive (map name (:inactive report))]
    (do
      (when (:help opts) (println banner))
      (println "Inactive:")
      (pprint inactive)
      (println "Used:")
      (pprint used))))
