package icu.debug.net.wg.client.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 节点状态请求
 */
@Getter
@Setter
@ToString
public class NodeStatusRequest {
    
    private boolean online;
    
} 