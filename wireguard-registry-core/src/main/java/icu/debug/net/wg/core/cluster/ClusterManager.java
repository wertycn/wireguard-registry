package icu.debug.net.wg.core.cluster;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 集群管理器
 * 负责集群节点发现、健康检查和数据同步
 */
@Slf4j
public class ClusterManager {

    private final ClusterNodeRegistry nodeRegistry;
    private final ClusterHealthChecker healthChecker;
    private final ScheduledExecutorService scheduler;
    private final String currentNodeId;
    private final ClusterMode mode;
    
    private static final long HEALTH_CHECK_INTERVAL = 10; // 秒
    private static final long NODE_DISCOVERY_INTERVAL = 30; // 秒

    public ClusterManager(String nodeId, ClusterMode mode, ClusterNodeRegistry nodeRegistry, ClusterHealthChecker healthChecker) {
        this.currentNodeId = nodeId;
        this.mode = mode;
        this.nodeRegistry = nodeRegistry;
        this.healthChecker = healthChecker;
        this.scheduler = Executors.newScheduledThreadPool(3);
    }

    /**
     * 启动集群管理器
     */
    public void start() {
        if (mode == ClusterMode.STANDALONE) {
            log.info("Starting in standalone mode, cluster management disabled");
            return;
        }

        log.info("Starting cluster manager for node: {}", currentNodeId);
        
        // 注册当前节点
        registerCurrentNode();
        
        // 启动定期任务
        startHealthCheck();
        startNodeDiscovery();
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        
        log.info("Cluster manager started successfully");
    }

    /**
     * 停止集群管理器
     */
    public void shutdown() {
        log.info("Shutting down cluster manager...");
        
        try {
            // 注销当前节点
            unregisterCurrentNode();
            
            // 停止定期任务
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            
            log.info("Cluster manager shut down successfully");
        } catch (Exception e) {
            log.error("Error during cluster manager shutdown", e);
        }
    }

    /**
     * 获取集群状态
     */
    public ClusterStatus getClusterStatus() {
        if (mode == ClusterMode.STANDALONE) {
            return new ClusterStatus(ClusterMode.STANDALONE, currentNodeId, List.of(), true);
        }

        try {
            List<ClusterNode> allNodes = nodeRegistry.getAllNodes();
            boolean isHealthy = healthChecker.isClusterHealthy();
            
            return new ClusterStatus(mode, currentNodeId, allNodes, isHealthy);
        } catch (Exception e) {
            log.error("Failed to get cluster status", e);
            return new ClusterStatus(mode, currentNodeId, List.of(), false);
        }
    }

    /**
     * 获取当前节点信息
     */
    public Optional<ClusterNode> getCurrentNode() {
        return nodeRegistry.getNode(currentNodeId);
    }

    /**
     * 获取所有活跃节点
     */
    public List<ClusterNode> getActiveNodes() {
        return nodeRegistry.getActiveNodes();
    }

    /**
     * 检查是否为集群模式
     */
    public boolean isClusterMode() {
        return mode == ClusterMode.CLUSTER;
    }

    /**
     * 检查当前节点是否为Leader（如果有Leader选举机制）
     */
    public boolean isLeader() {
        if (mode == ClusterMode.STANDALONE) {
            return true;
        }
        
        // 简单的Leader选举：取最早注册的活跃节点
        return getActiveNodes().stream()
                .min((n1, n2) -> n1.getRegisteredAt().compareTo(n2.getRegisteredAt()))
                .map(node -> node.getNodeId().equals(currentNodeId))
                .orElse(false);
    }

    /**
     * 注册当前节点
     */
    private void registerCurrentNode() {
        try {
            ClusterNode currentNode = new ClusterNode(
                    currentNodeId,
                    getLocalHostAddress(),
                    8080, // 默认端口
                    NodeStatus.HEALTHY,
                    Instant.now(),
                    Instant.now()
            );
            
            nodeRegistry.registerNode(currentNode);
            log.info("Registered current node: {}", currentNodeId);
        } catch (Exception e) {
            log.error("Failed to register current node", e);
        }
    }

    /**
     * 注销当前节点
     */
    private void unregisterCurrentNode() {
        try {
            nodeRegistry.unregisterNode(currentNodeId);
            log.info("Unregistered current node: {}", currentNodeId);
        } catch (Exception e) {
            log.error("Failed to unregister current node", e);
        }
    }

    /**
     * 启动健康检查
     */
    private void startHealthCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                performHealthCheck();
            } catch (Exception e) {
                log.error("Error during health check", e);
            }
        }, HEALTH_CHECK_INTERVAL, HEALTH_CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 启动节点发现
     */
    private void startNodeDiscovery() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                performNodeDiscovery();
            } catch (Exception e) {
                log.error("Error during node discovery", e);
            }
        }, NODE_DISCOVERY_INTERVAL, NODE_DISCOVERY_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 执行健康检查
     */
    private void performHealthCheck() {
        // 更新当前节点的最后心跳时间
        nodeRegistry.updateNodeHeartbeat(currentNodeId);
        
        // 检查其他节点的健康状态
        List<ClusterNode> allNodes = nodeRegistry.getAllNodes();
        for (ClusterNode node : allNodes) {
            if (!node.getNodeId().equals(currentNodeId)) {
                boolean isHealthy = healthChecker.checkNodeHealth(node);
                
                if (!isHealthy && node.getStatus() == NodeStatus.HEALTHY) {
                    log.warn("Node {} appears to be unhealthy", node.getNodeId());
                    nodeRegistry.updateNodeStatus(node.getNodeId(), NodeStatus.UNHEALTHY);
                } else if (isHealthy && node.getStatus() == NodeStatus.UNHEALTHY) {
                    log.info("Node {} recovered and is now healthy", node.getNodeId());
                    nodeRegistry.updateNodeStatus(node.getNodeId(), NodeStatus.HEALTHY);
                }
            }
        }
        
        // 清理过期节点
        nodeRegistry.cleanupExpiredNodes();
    }

    /**
     * 执行节点发现
     */
    private void performNodeDiscovery() {
        List<ClusterNode> discoveredNodes = nodeRegistry.discoverNodes();
        log.debug("Discovered {} cluster nodes", discoveredNodes.size());
        
        // 可以在这里添加更多的节点发现逻辑
        // 例如：通过配置文件、环境变量等发现新节点
    }

    /**
     * 获取本地主机地址
     */
    private String getLocalHostAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("Failed to get local host address, using localhost", e);
            return "localhost";
        }
    }

    /**
     * 集群模式枚举
     */
    public enum ClusterMode {
        STANDALONE, // 单机模式
        CLUSTER     // 集群模式
    }

    /**
     * 节点状态枚举
     */
    public enum NodeStatus {
        HEALTHY,    // 健康
        UNHEALTHY,  // 不健康
        OFFLINE     // 离线
    }
}