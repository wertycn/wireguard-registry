package icu.debug.net.wg.core;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import lombok.*;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-07 20:49
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class NetworkNodeConfigWrapper {

    private WireGuardNetworkNode node;

    private WireGuardIniConfig config;

    public String getHostName() {
        return node.getServerNode().getHostname();
    }
}
