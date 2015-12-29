(ns cli-64spec.core
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go thread chan close!]]
            [clj-commons-exec :as exec]
            [me.raynes.fs :as fs])
  (:gen-class))

(defn header-message
  [file-path message]
  (str "Testing: " file-path "\n"
       "  compilation: "
       message))

(def kick-ass-path "/Applications/KickAssembler/KickAss.jar")
(def output-dir ".")

(defn output-file-path [file-path]
  (str (fs/base-name file-path true) ".prg"))

(defn result-file-name [file-path]
  (str (fs/base-name file-path true) ".txt"))

(defn vice-symbols-file-name [file-path]
  (str (fs/base-name file-path true) ".vs"))

(defn prepare-kick-ass-params
  [file-path]
  ["java" "-jar" kick-ass-path
   file-path
   "-o" (output-file-path file-path)
   "-symbolfiledir" output-dir
   "-vicesymbols"
   ":on_exit=jam"
   ":write_final_results_to_file=true"
   (str  ":result_file_name=" (result-file-name file-path))])

(defn prepare-vice-params
  [file-path]
  ["x64" "-warp"
   "-autostart" (output-file-path file-path)
   "-moncommands" (vice-symbols-file-name file-path)
   "-autostartprgmode" "0"
   "-jamaction" "5"
   "-chdir" (str  "./" output-dir)])

(defn run-test
  [file-path]
  (let [{exit :exit error :err}
        @(exec/sh (prepare-vice-params file-path))]
    (str "  running: "
         (if (= exit 0)
           (slurp (result-file-name file-path))
           error))))

(defn compile-file
  [file-path]
  (let [{exit :exit output :out error :err}
        @(exec/sh (prepare-kick-ass-params file-path))]
    (str "  compilation: "
         (if (= exit 0)
           (str "ok\n" (run-test file-path))
           (str "\n" output error)))))

(defn test-file
  [channel file-path]
  (thread (>!! channel
          (str "Testing: " file-path "\n"
               (compile-file file-path)))
      (close! channel)))

(defn schedule-test
  [file-path]
  (let [channel (chan)]
    (test-file channel file-path)
    channel))

(defn -main
  [& args]
  (let [channels (map #(schedule-test %) args)]
    (println "Running " (count channels) "tests")
    (doall (map #(println (<!! %1)) channels))))
