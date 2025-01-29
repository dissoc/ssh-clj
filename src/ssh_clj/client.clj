(ns ssh-clj.client
  (:require
   [clojure.java.io :as io]
   [ssh-clj.forwarding :refer [forwarding-filter-all]])
  (:import
   (java.io ByteArrayOutputStream)
   (java.time Duration)
   (java.util EnumSet)
   (org.apache.sshd.client SshClient)
   (org.apache.sshd.client.channel ClientChannelEvent)
   (org.apache.sshd.client.session ClientSession)
   (org.apache.sshd.common.keyprovider KeyIdentityProvider)
   (org.apache.sshd.common.util.security SecurityUtils)
   (org.apache.sshd.core CoreModuleProperties)
   (org.apache.sshd.scp.client ScpClient ScpClientCreator)))


(defn execute-command
  [^ClientSession session
   ^String command
   & {:keys [timeout]
      :or   {timeout 10000}}]
  (let [channel (.createExecChannel session command)]
    (with-open [std-out (new ByteArrayOutputStream)
                std-err (new ByteArrayOutputStream)]
      (.setOut channel std-out)
      (.setErr channel std-err)
      (-> channel
          .open
          (.verify timeout))
      (-> channel
          (.waitFor (EnumSet/of ClientChannelEvent/CLOSED) 0))
      (String. (.toByteArray std-out)))))

(defn scp-download-bytes
  [^ScpClient scp-client remote-file-path]
  (.downloadBytes scp-client remote-file-path))

(defn ^ScpClient create-scp-client
  [^ClientSession session]
  (let [creator (ScpClientCreator/instance)]
    (.createScpClient creator session)))

(defn ^SshClient create-client
  [& {:keys [enable-forwarding? keepalive-interval]
      :or   {enable-forwarding  false
             keepalive-interval 30}}]
  (let [client (SshClient/setUpDefaultClient)]
    (when keepalive-interval
      (.set CoreModuleProperties/HEARTBEAT_INTERVAL
            client
            (Duration/ofSeconds keepalive-interval)))
    (when enable-forwarding?
      (.setForwardingFilter client (forwarding-filter-all)))
    (.start client)
    client))

(defn load-key-pair-identities [key-pair-path]
  (let [input-stream (-> key-pair-path
                         io/file
                         io/input-stream)]
    (SecurityUtils/loadKeyPairIdentities nil nil input-stream nil)))

;; TODO handle more key types
(defn ^ClientSession create-session
  [client host port
   & {:keys [user key-path keepalive-interval connect-timeout]
      :or   {key-path           (str (System/getProperty "user.home")
                                     "/.ssh/id_ed25519")
             user               (System/getProperty "user.name")
             keepalive-interval 30
             connect-timeout    30}}]
  (let [session (-> (.connect client user host port)
                    (doto
                        (.verify (* 1000 connect-timeout)))
                    .getSession)

        key-pair (load-key-pair-identities key-path)]

    (->> (KeyIdentityProvider/wrapKeyPairs key-pair)
         (.setKeyIdentityProvider session))

    (-> (.auth session)
        (.verify (* 1000 connect-timeout)))
    session))
