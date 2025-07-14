package icu.debug.net.wg.client.entity;

import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 节点注册请求
 */
@Getter
@Setter
@ToString
public class NodeRegistrationRequest {
    
    private WireGuardNetworkNode node;
    
} 