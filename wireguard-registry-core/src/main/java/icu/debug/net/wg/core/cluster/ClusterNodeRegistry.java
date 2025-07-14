package icu.debug.net.wg.core.cluster;

import java.util.List;
import java.util.Optional;

/**
 * 集群节点注册接口
 */
public interface ClusterNodeRegistry {

    /**
     * 注册节点
     */
    void registerNode(ClusterNode node);

    /**
     * 注销节点
     */
    void unregisterNode(String nodeId);

    /**
     * 获取节点信息
     */
    Optional<ClusterNode> getNode(String nodeId);

    /**
     * 获取所有节点
     */
    List<ClusterNode> getAllNodes();

    /**
     * 获取活跃节点
     */
    List<ClusterNode> getActiveNodes();

    /**
     * 更新节点心跳时间
     */
    void updateNodeHeartbeat(String nodeId);

    /**
     * 更新节点状态
     */
    void updateNodeStatus(String nodeId, ClusterManager.NodeStatus status);

    /**
     * 清理过期节点
     */
    void cleanupExpiredNodes();

    /**
     * 发现节点
     */
    List<ClusterNode> discoverNodes();
}