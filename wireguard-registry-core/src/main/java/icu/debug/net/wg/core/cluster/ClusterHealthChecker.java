package icu.debug.net.wg.core.cluster;

/**
 * 集群健康检查接口
 */
public interface ClusterHealthChecker {

    /**
     * 检查节点健康状态
     */
    boolean checkNodeHealth(ClusterNode node);

    /**
     * 检查集群整体健康状态
     */
    boolean isClusterHealthy();

    /**
     * 获取健康检查超时时间（秒）
     */
    long getHealthCheckTimeout();
}