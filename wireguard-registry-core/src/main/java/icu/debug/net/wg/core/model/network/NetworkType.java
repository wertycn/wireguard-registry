package icu.debug.net.wg.core.model.network;

public enum NetworkType {

    /**
     * OPEN NAT 开放式局域网(只有一个公网地址，后面由多台设备,每台设备都可以由独立端口与公网交互)
     */
    OPEN_NAT,

    /**
     * BRIDGE NAT 中继局域网(只有一个公网地址，后面由多台设备,每台设备都需要通过中继服务器才能与公网交互)
     * 该类型下，第一个节点为中继服务器，后面的节点为内网设备
     */
    BRIDGE_NAT,


    /**
     * CLOUD 公有云(每天主机都有自己的公网IP,同时主机之间可以通过内网通信)
     */
    CLOUD,

    /**
     * 英特网 (特殊形式，最大的局域网，相当于多个只有一台主机的CLOUD或OPEN_LAN)
     */
    WAN,

    ;

}
