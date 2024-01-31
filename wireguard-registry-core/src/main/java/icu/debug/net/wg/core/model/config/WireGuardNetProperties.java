package icu.debug.net.wg.core.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-01-29 2:51
 */
@Getter
@Setter
@ToString
public class WireGuardNetProperties {


    /**
     * 子网地址
     */
    private String address;

    /**
     * 子网掩码
     */
    private String netmask;

    /**
     * 监听端口
     * <p>
     * 当本地节点是中继服务器，需要通过该参数指定端口来监听传入 VPN 连接，默认端口号是 51820。
     * 常规客户端不需要此选项。
     * </p>
     */
    private Integer listenPort;


    /**
     * DNS
     *
     * <p>
     * 通过 DHCP 向客户端宣告 DNS 服务器。客户端将会使用这里指定的 DNS 服务器来处理 VPN 子网中的 DNS 请求，但也可以在系统中覆盖此选项。例如：
     * 如果不配置则使用系统默认 DNS
     * 可以指定单个 DNS：DNS = 1.1.1.1
     * 也可以指定多个 DNS：DNS = 1.1.1.1,8.8.8.8
     * </p>
     */
    private List<String> dns;

    /**
     * 子网使用的路由表
     * <p>
     * 默认不需要设置。该参数有两个特殊的值需要注意：
     * Table = off : 禁止创建路由
     * Table = auto（默认值） : 将路由添加到系统默认的 table 中，并启用对默认路由的特殊处理。
     * </p>
     */
    private String table;

    /**
     * 定义连接到对等节点（peer）的 MTU（Maximum Transmission Unit，最大传输单元），默认不需要设置，一般由系统自动确
     */
    private Integer mtu;

    /**
     * 启动 VPN 接口之前运行的命令。这个选项可以指定多次，按顺序执行
     * <p>
     * eg: PreUp = ip rule add ipproto tcp dport 22 table 1234
     * </p>
     */
    private List<String> preUp;

    /**
     * 启动 VPN 接口之后运行的命令。这个选项可以指定多次，按顺序执行
     */
    private List<String> postUp;

    /**
     * 关闭 VPN 接口之前运行的命令。这个选项可以指定多次，按顺序执行
     */
    private List<String> preDown;

    /**
     * 关闭 VPN 接口之后运行的命令。这个选项可以指定多次，按顺序执行
     */
    private List<String> postDown;


}
