(defproject jenkins-plugins-manager "0.1.0"
  :description "Determine the used plugins in jenkins"
  :url "https://github.com/mateoconfeugo/jenkins-plugins-manager"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [prismatic/plumbing "0.3.3"] ;; function graphs
                 [shoreleave "0.3.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]]
  :plugins [[lein-expectations "0.0.7"]
            [lein-autoexpect "0.2.5"]]
  :profiles  {:dev {:dependencies [[expectations "1.4.49"]]}}
  :main jenkins-plugins-manager.core)
