(set-env!
 :source-paths #{"src" "resources" "test"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha10" :scope "provided"]
                 [adzerk/boot-reload "0.4.8" :scope "test"]
                 [adzerk/boot-test "1.1.1" :scope "test"]
                 [metosin/boot-alt-test "0.1.2" :scope "test"]
                 [org.senatehouse/expect-call "0.1.0" :scope "test"]
                 [clj-http-lite "0.3.0" :scope "compile"]
                 [cheshire "5.6.3" :scope "compile"]
                 [io.aviso/config "0.2.1" :scope "compile"]])

(require
 '[boot.task.built-in          :refer [aot]]
 '[adzerk.boot-reload          :refer [reload]]
 '[adzerk.boot-test            :refer [test]]
 '[metosin.boot-alt-test       :refer [alt-test]])

;;clojure namespace tools integration
(swap! boot.repl/*default-dependencies* conj
      '[org.clojure/tools.namespace "0.3.0-alpha3"])

;; atom-proto-repl dependency
(swap! boot.repl/*default-dependencies* conj
      '[compliment "0.2.7"])

;; CIDER integration
(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.12.0-SNAPSHOT"]
                [refactor-nrepl "2.0.0-SNAPSHOT"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)

;; Light Table integration
(swap! boot.repl/*default-dependencies*
       concat '[[lein-light-nrepl "0.3.2"]])
(swap! boot.repl/*default-middleware*
       conj 'lighttable.nrepl.handler/lighttable-ops)

;; Tasks
(deftask build []
  (comp (aot)))

(deftask dev
  []
  (comp (watch)
        (repl)
        (reload)
        (build)))

(deftask run-tests
  [a autotest bool "If no exception should be thrown when tests fail"]
  (comp
   (alt-test :fail (not autotest))))

(deftask autotest []
  (comp
   (watch)
   (run-tests :autotest true)))
