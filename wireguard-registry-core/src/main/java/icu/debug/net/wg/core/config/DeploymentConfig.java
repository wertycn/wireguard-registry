package icu.debug.net.wg.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 部署配置
 * 
 * @author Administrator
 * @since 2024-01-01
 */
public class DeploymentConfig {
    
    private static final Logger log = LoggerFactory.getLogger(DeploymentConfig.class);
    
    private final DeploymentMode mode;
    private final String nodeId;
    
    public DeploymentConfig(DeploymentMode mode, String nodeId) {
        this.mode = mode;
        this.nodeId = nodeId;
        
        validateConfiguration();
    }
    
    /**
     * 验证配置
     */
    private void validateConfiguration() {
        if (mode == null) {
            throw new IllegalArgumentException("Deployment mode cannot be null");
        }
        
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        
        log.info("Deployment configuration validated: mode={}, nodeId={}", mode, nodeId);
    }
    
    /**
     * 是否为单机模式
     */
    public boolean isStandalone() {
        return mode == DeploymentMode.STANDALONE;
    }
    
    /**
     * 是否为集群模式
     */
    public boolean isCluster() {
        return mode == DeploymentMode.CLUSTER;
    }
    
    /**
     * 是否允许使用内存存储
     */
    public boolean isMemoryStorageAllowed() {
        return mode == DeploymentMode.STANDALONE;
    }
    
    /**
     * 是否需要分布式存储
     */
    public boolean requiresDistributedStorage() {
        return mode == DeploymentMode.CLUSTER;
    }
    
    public DeploymentMode getMode() {
        return mode;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
}