package icu.debug.net.wg.core.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 集群状态数据类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterStatus {

    /**
     * 集群模式
     */
    private ClusterManager.ClusterMode mode;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 所有节点列表
     */
    private List<ClusterNode> nodes;

    /**
     * 集群是否健康
     */
    private boolean healthy;

    /**
     * 获取活跃节点数量
     */
    public int getActiveNodeCount() {
        return (int) nodes.stream().filter(ClusterNode::isActive).count();
    }

    /**
     * 获取总节点数量
     */
    public int getTotalNodeCount() {
        return nodes.size();
    }

    /**
     * 检查是否为单机模式
     */
    public boolean isStandaloneMode() {
        return mode == ClusterManager.ClusterMode.STANDALONE;
    }

    /**
     * 检查是否为集群模式
     */
    public boolean isClusterMode() {
        return mode == ClusterManager.ClusterMode.CLUSTER;
    }
}