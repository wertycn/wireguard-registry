package icu.debug.net.wg.core.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.function.Function;

/**
 * WireGuard 接口定义
 * <p>
 * 示例配置文件
 * [Interface]
 * # Name = node1.example.tld
 * Address = 192.0.2.3/32
 * ListenPort = 51820
 * PrivateKey = localPrivateKeyAbcAbcAbc=
 * DNS = 1.1.1.1,8.8.8.8
 * Table = 12345
 * MTU = 1500
 * PreUp = /bin/example arg1 arg2 %i
 * PostUp = /bin/example arg1 arg2 %i
 * PreDown = /bin/example arg1 arg2 %i
 * PostDown = /bin/example arg1 arg2 %i
 * </p>
 */
@Getter
@Setter
@ToString
public class WireGuardInterface {

    private static final Map<String, Function<WireGuardInterface, String>> FORMAT_SINGLE_FUNC_MAT = new LinkedHashMap<>();

    static {
        FORMAT_SINGLE_FUNC_MAT.put("# Name", WireGuardInterface::getName);
        FORMAT_SINGLE_FUNC_MAT.put("Address", WireGuardInterface::getAddress);
        FORMAT_SINGLE_FUNC_MAT.put("ListenPort", item -> Optional.ofNullable(item.getListenPort()).map(String::valueOf).orElse(""));
        FORMAT_SINGLE_FUNC_MAT.put("PrivateKey", WireGuardInterface::getPrivateKey);
        FORMAT_SINGLE_FUNC_MAT.put("DNS", item -> Optional.ofNullable(item.getDns()).map(v -> String.join(",", v)).orElse(""));
        FORMAT_SINGLE_FUNC_MAT.put("Table", WireGuardInterface::getTable);
        FORMAT_SINGLE_FUNC_MAT.put("MTU", item -> Optional.ofNullable(item.getMtu()).map(String::valueOf).orElse(""));
    }

    private static final Map<String, Function<WireGuardInterface, List<String>>> FORMAT_MULTI_FUNC_MAT = new LinkedHashMap<>();

    static {
        FORMAT_MULTI_FUNC_MAT.put("PreUp", WireGuardInterface::getPreUp);
        FORMAT_MULTI_FUNC_MAT.put("PostUp", WireGuardInterface::getPostUp);
        FORMAT_MULTI_FUNC_MAT.put("PreDown", WireGuardInterface::getPreDown);
        FORMAT_MULTI_FUNC_MAT.put("PostDown", WireGuardInterface::getPostDown);
    }


    /**
     * 名称
     */
    private String name;

    /**
     * 本地节点地址
     * <p>
     * 定义本地节点应该对哪个地址范围进行路由。
     * 常规的客户端，则将其设置为节点本身的单个 IP（使用 CIDR 指定，例如 192.0.2.3/32）；
     * 中继服务器，则将其设置为可路由的子网范围。
     * </p>
     */
    private String address;

    /**
     * 监听端口
     * <p>
     * 当本地节点是中继服务器，需要通过该参数指定端口来监听传入 VPN 连接，默认端口号是 51820。
     * 常规客户端不需要此选项。
     * </p>
     */
    private Integer listenPort;

    /**
     * 本地节点的私钥
     * <p>
     * 所有节点（包括中继服务器）都必须设置。不可与其他服务器共用。
     * 私钥可通过wg genkey命令生成。
     * </p>
     */
    private String privateKey;

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


    public String toIniString() {
        List<String> content = new ArrayList<>();
        content.add("[Interface]");
        FORMAT_SINGLE_FUNC_MAT.forEach((key, func) -> {
            String value = func.apply(this);
            if (Objects.nonNull(value) && !value.isEmpty()) {
                content.add(key + " = " + value);
            }
        });
        FORMAT_MULTI_FUNC_MAT.forEach((key, func) -> {
            List<String> values = func.apply(this);
            if (Objects.nonNull(values) && !values.isEmpty()) {
                values.stream().filter(Objects::nonNull)
                        .filter(value -> !value.isEmpty())
                        .forEach(value -> content.add(key + " = " + value));
            }
        });
        return String.join("\n", content);
    }

}

