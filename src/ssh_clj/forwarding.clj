(ns ssh-clj.forwarding
  (:import
   (org.apache.sshd.common.forward PortForwardingEventListener)
   (org.apache.sshd.server.forward ForwardingFilter)))

;; TODO support custom filtering
(defn forwarding-filter-all []
  (proxy [ForwardingFilter] []
    (canForwardX11 [session]
      true)
    (canForwardAgent [session]
      true)
    (canConnect [address session]
      true)
    (canListen [address session]
      true)))

(defn port-forwarding-event-listener
  [& {:keys [on-establishing-explicit-tunnel
             on-established-explicit-tunnel
             on-tearing-down-explicit-tunnel
             on-torn-down-explicit-tunnel
             on-establishing-dynamic-tunnel
             on-established-dynamic-tunnel
             on-tearing-down-dynamic-tunnel
             torn-down-dynamic-tunnel]}]

  (reify PortForwardingEventListener

    ;; Signals the attempt to establish a local/remote port forwarding
    (establishingExplicitTunnel
        [this session local remote local-forwarding]
      (when on-establishing-explicit-tunnel
        (on-establishing-explicit-tunnel
         [this session local remote local-forwarding])))

    ;; Signals a successful/failed attempt to establish a local/remote
    ;; port forwarding
    (establishedExplicitTunnel
        [this session local remote local-forwarding bound-address reason]
      (when on-established-explicit-tunnel
        (on-established-explicit-tunnel
         [this session local remote local-forwarding bound-address reason])))

    ;; Signals a request to tear down a local/remote port forwarding
    (tearingDownExplicitTunnel
        [this session address local-forwarding remote-address]
      (when on-tearing-down-explicit-tunnel
        (on-tearing-down-explicit-tunnel
         [this session address local-forwarding remote-address])))

    ;; Signals a successful/failed request to tear down a local/remote
    ;; port forwarding
    (tornDownExplicitTunnel
        [this session address local-forwarding remote-address reason]
      (when on-torn-down-explicit-tunnel
        (on-torn-down-explicit-tunnel
         [this session address local-forwarding remote-address reason])))

    ;; Signals the attempt to establish a dynamic port forwarding
    (establishingDynamicTunnel
        [this session local]
      (when on-establishing-dynamic-tunnel
        (on-establishing-dynamic-tunnel [this session local])))

    ;; Signals a successful/failed attempt to establish a dynamic
    ;; port forwarding
    (establishedDynamicTunnel
        [this session local bound-address reason]
      (when on-established-dynamic-tunnel
        (on-established-dynamic-tunnel
         [this session local bound-address reason])))

    ;; Signals a request to tear down a dynamic forwarding
    (tearingDownDynamicTunnel
        [this session address]
      (when on-tearing-down-dynamic-tunnel
        (on-tearing-down-dynamic-tunnel [this session address])))

    ;;Signals a successful/failed request to tear down a dynamic port forwarding
    (tornDownDynamicTunnel
        [this session address reason]
      (when torn-down-dynamic-tunnel
        (torn-down-dynamic-tunnel [this session address reason])))))
