(ns ssh-clj.client
  (:import
   (java.time Duration)
   (org.apache.sshd.client SshClient)
   (org.apache.sshd.core CoreModuleProperties)))


(defn create-client
  [& {:keys [enable-forwarding? keepalive-interval]
      :or   {enable-forwarding  false
             keepalive-interval 30}}]
  (let [client (SshClient/setUpDefaultClient)]
    (when keepalive-interval
      (.set CoreModuleProperties/HEARTBEAT_INTERVAL
            client
            (Duration/ofSeconds keepalive-interval)))
    (.start client)
    client))
