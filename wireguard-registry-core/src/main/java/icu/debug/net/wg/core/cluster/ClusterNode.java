package icu.debug.net.wg.core.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 集群节点数据类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterNode {

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点地址
     */
    private String address;

    /**
     * 节点端口
     */
    private int port;

    /**
     * 节点状态
     */
    private ClusterManager.NodeStatus status;

    /**
     * 注册时间
     */
    private Instant registeredAt;

    /**
     * 最后心跳时间
     */
    private Instant lastHeartbeat;

    /**
     * 节点元数据（可选）
     */
    private String metadata;

    public ClusterNode(String nodeId, String address, int port, ClusterManager.NodeStatus status, Instant registeredAt, Instant lastHeartbeat) {
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
        this.status = status;
        this.registeredAt = registeredAt;
        this.lastHeartbeat = lastHeartbeat;
    }

    /**
     * 获取节点的完整地址
     */
    public String getFullAddress() {
        return address + ":" + port;
    }

    /**
     * 检查节点是否活跃
     */
    public boolean isActive() {
        return status == ClusterManager.NodeStatus.HEALTHY;
    }

    /**
     * 检查节点是否过期（超过指定时间没有心跳）
     */
    public boolean isExpired(long timeoutSeconds) {
        if (lastHeartbeat == null) {
            return true;
        }
        return Instant.now().getEpochSecond() - lastHeartbeat.getEpochSecond() > timeoutSeconds;
    }
}