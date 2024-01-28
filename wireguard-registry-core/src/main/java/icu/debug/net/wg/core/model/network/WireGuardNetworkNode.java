package icu.debug.net.wg.core.model.network;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
}
