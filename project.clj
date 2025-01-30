(defproject ssh-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[net.i2p.crypto/eddsa "0.3.0"]
                 [org.apache.sshd/sshd-core "2.14.0"]
                 [org.apache.sshd/sshd-scp "2.14.0"]
                 [org.clojure/clojure "1.11.1"]]
  :repl-options {:init-ns ssh-clj.client})
