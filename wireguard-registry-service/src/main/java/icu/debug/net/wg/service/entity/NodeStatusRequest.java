package icu.debug.net.wg.service.entity;

import lombok.Data;

/**
 * 节点状态请求实体
 */
@Data
public class NodeStatusRequest {
    
    private boolean online;
    
    public NodeStatusRequest() {}
    
    public NodeStatusRequest(boolean online) {
        this.online = online;
    }
}