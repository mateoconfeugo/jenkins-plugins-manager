(ns jenkins-plugins-manager.test.core
  ^ {:author "Matthew Burns"
     :doc "Test the jenkins plugin reporting"}
    (:require [expectations :refer [expect]]
              [jenkins-plugins-manager.core :refer :all]))

(def test-data (-main))
(def test-plugins-used (:used test-data))
(def test-plugins-inactive (:inactive test-data))

[:external-monitor-job :jquery :javadoc :run-condition :scm-api :jira :pam-auth :translation :jenkins-cloudformation-plugin :matrix-auth :cvs :ldap :token-macro :node-iterator-api :jbehave-hudson-plugin]

[:git-client :port-allocator :plot :htmlpublisher :ec2 :github-api :github :repository :conditional-buildstep :credentials :promoted-builds :performance :git :active-directory :copyartifact :clone-workspace-scm :xunit :project-build-times :subversion :job-dsl :pmd :jenkins-multijob-plugin :ssh-slaves :maven-plugin :build-pipeline-plugin :dry  :mailer :ssh-credentials :description-setter :groovy-postbuild :analysis-core :nodelabelparameter :ant :dashboard-view :jdepend :envinject :repository-connector :cloverphp :parameterized-trigger :locks-and-latches :violations :xvfb :cobertura :ghprb :checkstyle :s3 :jacoco]

(expect true (< 0 (count (:used test-data))))
