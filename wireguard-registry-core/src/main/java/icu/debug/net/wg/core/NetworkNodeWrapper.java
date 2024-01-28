package icu.debug.net.wg.core;

import icu.debug.net.wg.core.model.config.WireGuardInterface;
import icu.debug.net.wg.core.model.config.WireGuardPeer;
import icu.debug.net.wg.core.model.network.EndpointType;
import icu.debug.net.wg.core.model.network.NetworkType;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 包装网络节点配置相关信息的对象
 * <p>
 * 用于生成最终配置的中间对象
 * </p>
 *
 * @author hanjinxiang@debug.icu
 * @date 2024-01-29 1:53
 */
@Getter
@Setter
@ToString
public class NetworkNodeWrapper {

    private WireGuardNetworkNode node;

    private String localAreaNetwork;

    private boolean bridge;

    private NetworkType networkType;

    public WireGuardInterface toInterface() {
        return node.toInterface();
    }

    public EndpointType getEndpointType(NetworkNodeWrapper request) {
        // TODO : client 模型支持， bridge 抽象为节点类型

        // 同一局域网，且不是中继模式 -> 内网
        if (!this.localAreaNetwork.equals(request.getLocalAreaNetwork())) {
            return EndpointType.PUBLIC;
        }
        if (this.localAreaNetwork.equals(request.getLocalAreaNetwork()) && !isBridge()) {
            return EndpointType.PRIVATE;
        }

        return EndpointType.UNKNOWN;
    }

    public WireGuardPeer buildPeer(NetworkNodeWrapper requestWrapper) {
        EndpointType endpointType = this.getEndpointType(requestWrapper);
        return this.getNode().toPeer(endpointType);
    }
}
