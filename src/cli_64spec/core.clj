(ns cli-64spec.core
  (:require [clojure.core.async
             :as async
             :refer [>!! <!! thread chan close!]]
            [clj-commons-exec :as exec]
            [me.raynes.fs :as fs])
  (:gen-class))

(def kick-ass-path "/Applications/KickAssembler/KickAss.jar")

(def output-dir ".")

(defn output-file-path
  [file-path]
  (str (fs/base-name file-path true) ".prg"))

(defn result-file-name
  [file-path]
  (str (fs/base-name file-path true) ".txt"))

(defn vice-symbols-file-name
  [file-path]
  (str (fs/base-name file-path true) ".vs"))

(defn kick-assembler-args
  "Builds arguments for assembler."
  [file-path]
  ["java" "-jar" kick-ass-path
   file-path
   "-o" (output-file-path file-path)
   "-symbolfiledir" output-dir
   "-vicesymbols"
   ":on_exit=jam"
   ":write_final_results_to_file=true"
   (str  ":result_file_name=" (result-file-name file-path))])

(defn vice-args
  "Builds arguments for emulator."
  [file-path]
  ["x64" "-warp"
   "-autostart" (output-file-path file-path)
   "-moncommands" (vice-symbols-file-name file-path)
   "-autostartprgmode" "0"
   "-jamaction" "5"
   "-chdir" (str  "./" output-dir)])

(defn run-vice
  "Runs test file on emulator."
  [file-path]
  @(exec/sh (vice-args file-path)))

(defn run-kick-assembler
  "Runs assembler in order to compile test."
  [file-path]
  @(exec/sh (kick-assembler-args file-path)))

(defn read-result-file
  "Reads result file"
  [file-path]
  ;; TODO convert PETSCII to ASCII
  (slurp (result-file-name file-path)))

(defn run-test
  "Runs test file with emulator."
  [file-path]
  (let [{exit :exit err :err} (run-vice file-path)]
    (str "  running: " (if (= exit 0)
                         (read-result-file file-path)
                         err))))

(defn compile-test-and-run-it
  "Compiles test file and run it with emultor."
  [file-path]
  (let [{exit :exit out :out err :err}
        (run-kick-assembler file-path)]
    (str "  compilation: " (if (= exit 0)
                             (str "ok\n" (run-test file-path))
                             (str "\n" out err)))))

(defn process-file
  "Process file in seperate thread with closing channel after its done."
  [channel file-path]
  (thread (>!! channel
               (str "Testing: " file-path "\n"
                    (compile-test-and-run-it file-path)))
          (close! channel)))

(defn setup-test
  "Setup processing of file with new channel, returns channel."
  [file-path]
  (let [channel (chan)]
    (process-file channel file-path)
    channel))

(defn -main
  [& args]
  (let [files (filter #(.exists (clojure.java.io/file %)) args)
        channels (map #(setup-test %) files)]
    (println "Running" (count channels) "tests")
    (doall (map #(println (<!! %1)) channels))))
