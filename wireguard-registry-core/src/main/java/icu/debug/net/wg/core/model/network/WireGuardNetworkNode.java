package icu.debug.net.wg.core.model.network;

import icu.debug.net.wg.core.helper.WireGuardGenKeyHelper;
import icu.debug.net.wg.core.model.config.Endpoint;
import icu.debug.net.wg.core.model.config.WireGuardInterface;
import icu.debug.net.wg.core.model.config.WireGuardPeer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * WireGuard 网络节点 对应Interface
 */
@Getter
@Setter
@ToString
public class WireGuardNetworkNode {

    private ServerNode serverNode;

    /**
     * WireGuard 组网网络的的IP地址
     */
    private String address;


    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;

    private Integer listenPort;

    private List<String> dns;

    private String table;

    /**
     * 定义连接到对等节点（peer）的 MTU（Maximum Transmission Unit，最大传输单元），默认不需要设置，一般由系统自动确
     */
    private Integer mtu;

    /**
     * 启动接口之前运行的命令。这个选项可以指定多次，按顺序执行
     * <p>
     * eg: PreUp = ip rule add ipproto tcp dport 22 table 1234
     * </p>
     */
    private List<String> preUp;

    /**
     * 启动接口之后运行的命令。这个选项可以指定多次，按顺序执行
     */
    private List<String> postUp;

    /**
     * 关闭接口之前运行的命令。这个选项可以指定多次，按顺序执行
     */
    private List<String> preDown;

    /**
     * 关闭接口之后运行的命令。这个选项可以指定多次，按顺序执行
     */
    private List<String> postDown;

    public WireGuardInterface toInterface() {
        WireGuardInterface wireGuardInterface = new WireGuardInterface();
        Assert.notNull(getServerNode(), "ServerNode it must not null");
        wireGuardInterface.setName(getServerNode().getHostname());
        wireGuardInterface.setAddress(address);
        wireGuardInterface.setDns(dns);
        wireGuardInterface.setListenPort(listenPort);
        wireGuardInterface.setMtu(mtu);
        wireGuardInterface.setPostDown(postDown);
        wireGuardInterface.setPostUp(postUp);
        wireGuardInterface.setPreDown(preDown);
        wireGuardInterface.setPreUp(preUp);
        wireGuardInterface.setPrivateKey(privateKey);
        wireGuardInterface.setTable(table);
        return wireGuardInterface;
    }

    public WireGuardPeer toPeer(EndpointType endpointType) {
        WireGuardPeer peer = new WireGuardPeer();
        peer.setName(serverNode.getHostname());
        EndpointType.buildEndpoint(endpointType, this.serverNode, this.listenPort)
                .map(Endpoint::toString)
                .ifPresent(peer::setEndpoint);
        if (privateKey != null) {
            peer.setPublicKey(WireGuardGenKeyHelper.genPubKeyByPrivateKey(privateKey));
        }
        if (!ObjectUtils.isEmpty(address)) {
            peer.setAllowedIPs(List.of(address));
        }
        return peer;
    }
}
