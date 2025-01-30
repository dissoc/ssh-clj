(ns ssh-clj.core-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer :all]
   [ssh-clj.client :refer :all])
  (:import
   (java.io PushbackReader)
   (org.apache.sshd.client SshClient)))


(def config (with-open [rdr  (-> "test-config.edn"
                                 io/file
                                 io/reader
                                 PushbackReader.)]
              (clojure.edn/read rdr)))

(deftest client-test
  (testing "creating client"
    (let [client (create-client)]
      (is (instance? SshClient client)))))

(deftest ssh-client-session-test
  (testing "testing ssh client creation"
    (let [client  (create-client)
          session (create-session client
                                  (-> config :remote :host)
                                  (-> config :remote :port))]
      (is (.isOpen session))

      (testing "testing-command"
        (let [response (execute-command session "echo test")]
          (is (= response "test\n")))))))
