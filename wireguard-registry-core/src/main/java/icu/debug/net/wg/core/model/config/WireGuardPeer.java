package icu.debug.net.wg.core.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.function.Function;

/**
 * 定义能够为一个或多个地址路由流量的对等节点（peer）的 VPN 设置。
 * <p>
 * 对等节点（peer）可以是将流量转发到其他对等节点（peer）的中继服务器，也可以是通过公网或内网直连的客户端
 * [Peer]
 * # Name = public-server1.example-vpn.tld
 * Endpoint = public-server1.example-vpn.tld:51820
 * PublicKey = <public key for public-server1.example-vpn.tld>
 * # 路由整个 VPN 子网的流量
 * AllowedIPs = 192.0.2.1/24
 * PersistentKeepalive = 25
 *
 * </p>
 */
@Getter
@Setter
@ToString
public class WireGuardPeer {

    private final static Map<String, Function<WireGuardPeer,String>> FORMAT_SINGLE_FUNC_MAT = new LinkedHashMap<>(){{
        put("# Name", WireGuardPeer::getName);
        put("Endpoint", WireGuardPeer::getEndpoint);
        put("PublicKey", WireGuardPeer::getPublicKey);
        put("AllowedIPs", item-> Optional.ofNullable(item.getAllowedIPs()).map(v->String.join(",",v)).orElse(""));
        put("PersistentKeepalive", item-> Optional.ofNullable(item.getPersistentKeepalive()).map(String::valueOf).orElse(""));
    }};


    /**
     * 配置说明
     */
    private String name;

    /**
     * 允许该对等节点（peer）发送过来的 VPN 流量中的源地址范围。同时这个字段也会作为本机路由表中 wg0 绑定的 IP 地址范围。如果对等节点（peer）是常规的客户端，则将其设置为节点本身的单个 IP；如果对等节点（peer）是中继服务器，则将其设置为可路由的子网范围。
     * 指定多个 IP 或子网范围
     */
    private List<String> allowedIPs;

    /**
     * 指定远端对等节点（peer）的公网地址。
     * <p>
     * 如果对等节点（peer）位于 NAT 后面或者没有稳定的公网访问地址，就忽略这个字段。
     * 通常只需要指定中继服务器的 Endpoint，当然有稳定公网 IP 的节点也可以指定
     * </p>
     */
    private String endpoint;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 连通性检查时间间隔
     * <p>
     * 如果连接是从一个位于 NAT 后面的对等节点（peer）到一个公网可达的对等节点（peer），
     * 那么 NAT 后面的对等节点（peer）必须定期发送一个出站 ping 包来检查连通性，
     * 如果 IP 有变化，就会自动更新Endpoint
     * </p>
     */
    private Integer persistentKeepalive;

    public String toIniString() {
        List<String> content = new ArrayList<>();
        content.add("[Peer]");
        FORMAT_SINGLE_FUNC_MAT.forEach((key, func) -> {
            String value = func.apply(this);
            if (Objects.nonNull(value) && !value.isEmpty()) {
                content.add(key + " = " + value);
            }
        });
        return String.join("\n",content);
    }
}

