package icu.debug.net.wg.service.entity;

import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import lombok.Data;

/**
 * 节点注册请求实体
 */
@Data
public class NodeRegistrationRequest {
    
    private WireGuardNetworkNode node;
    
    public NodeRegistrationRequest() {}
    
    public NodeRegistrationRequest(WireGuardNetworkNode node) {
        this.node = node;
    }
}