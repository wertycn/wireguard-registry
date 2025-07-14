package icu.debug.net.wg.core.cluster.impl;

import icu.debug.net.wg.core.cluster.ClusterManager;
import icu.debug.net.wg.core.cluster.ClusterNode;
import icu.debug.net.wg.core.cluster.ClusterNodeRegistry;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据库版本的集群节点注册实现
 * 适用于集群模式，数据共享
 */
@Slf4j
public class DatabaseClusterNodeRegistry implements ClusterNodeRegistry {

    private final ClusterNodeDao clusterNodeDao;
    private static final long NODE_TIMEOUT_SECONDS = 60; // 节点超时时间

    public DatabaseClusterNodeRegistry(ClusterNodeDao clusterNodeDao) {
        this.clusterNodeDao = clusterNodeDao;
    }

    @Override
    public void registerNode(ClusterNode node) {
        try {
            ClusterNodeEntity entity = convertToEntity(node);
            
            Optional<ClusterNodeEntity> existing = clusterNodeDao.findByNodeId(node.getNodeId());
            if (existing.isPresent()) {
                ClusterNodeEntity existingEntity = existing.get();
                existingEntity.setAddress(node.getAddress());
                existingEntity.setPort(node.getPort());
                existingEntity.setStatus(node.getStatus().name());
                existingEntity.setLastHeartbeat(LocalDateTime.ofInstant(node.getLastHeartbeat(), ZoneId.systemDefault()));
                existingEntity.setMetadata(node.getMetadata());
                existingEntity.setUpdatedAt(LocalDateTime.now());
                clusterNodeDao.update(existingEntity);
            } else {
                clusterNodeDao.save(entity);
            }
            
            log.debug("Registered cluster node: {}", node.getNodeId());
        } catch (Exception e) {
            log.error("Failed to register cluster node: {}", node.getNodeId(), e);
            throw new RuntimeException("Failed to register cluster node", e);
        }
    }

    @Override
    public void unregisterNode(String nodeId) {
        try {
            clusterNodeDao.deleteByNodeId(nodeId);
            log.debug("Unregistered cluster node: {}", nodeId);
        } catch (Exception e) {
            log.error("Failed to unregister cluster node: {}", nodeId, e);
            throw new RuntimeException("Failed to unregister cluster node", e);
        }
    }

    @Override
    public Optional<ClusterNode> getNode(String nodeId) {
        try {
            return clusterNodeDao.findByNodeId(nodeId)
                    .map(this::convertToClusterNode);
        } catch (Exception e) {
            log.error("Failed to get cluster node: {}", nodeId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<ClusterNode> getAllNodes() {
        try {
            return clusterNodeDao.findAll()
                    .stream()
                    .map(this::convertToClusterNode)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all cluster nodes", e);
            return List.of();
        }
    }

    @Override
    public List<ClusterNode> getActiveNodes() {
        try {
            return clusterNodeDao.findByStatus(ClusterManager.NodeStatus.HEALTHY.name())
                    .stream()
                    .map(this::convertToClusterNode)
                    .filter(node -> !node.isExpired(NODE_TIMEOUT_SECONDS))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get active cluster nodes", e);
            return List.of();
        }
    }

    @Override
    public void updateNodeHeartbeat(String nodeId) {
        try {
            Optional<ClusterNodeEntity> entityOpt = clusterNodeDao.findByNodeId(nodeId);
            if (entityOpt.isPresent()) {
                ClusterNodeEntity entity = entityOpt.get();
                entity.setLastHeartbeat(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                clusterNodeDao.update(entity);
                log.debug("Updated heartbeat for cluster node: {}", nodeId);
            }
        } catch (Exception e) {
            log.error("Failed to update heartbeat for cluster node: {}", nodeId, e);
        }
    }

    @Override
    public void updateNodeStatus(String nodeId, ClusterManager.NodeStatus status) {
        try {
            Optional<ClusterNodeEntity> entityOpt = clusterNodeDao.findByNodeId(nodeId);
            if (entityOpt.isPresent()) {
                ClusterNodeEntity entity = entityOpt.get();
                entity.setStatus(status.name());
                entity.setUpdatedAt(LocalDateTime.now());
                clusterNodeDao.update(entity);
                log.debug("Updated status for cluster node: {} to {}", nodeId, status);
            }
        } catch (Exception e) {
            log.error("Failed to update status for cluster node: {}", nodeId, e);
        }
    }

    @Override
    public void cleanupExpiredNodes() {
        try {
            LocalDateTime expiredBefore = LocalDateTime.now().minusSeconds(NODE_TIMEOUT_SECONDS);
            int removedCount = clusterNodeDao.deleteExpiredNodes(expiredBefore);
            if (removedCount > 0) {
                log.debug("Cleaned up {} expired cluster nodes", removedCount);
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired cluster nodes", e);
        }
    }

    @Override
    public List<ClusterNode> discoverNodes() {
        // 数据库实现中，发现节点就是获取所有节点
        return getAllNodes();
    }

    /**
     * 转换业务对象到实体
     */
    private ClusterNodeEntity convertToEntity(ClusterNode node) {
        ClusterNodeEntity entity = new ClusterNodeEntity();
        entity.setNodeId(node.getNodeId());
        entity.setAddress(node.getAddress());
        entity.setPort(node.getPort());
        entity.setStatus(node.getStatus().name());
        entity.setRegisteredAt(LocalDateTime.ofInstant(node.getRegisteredAt(), ZoneId.systemDefault()));
        entity.setLastHeartbeat(LocalDateTime.ofInstant(node.getLastHeartbeat(), ZoneId.systemDefault()));
        entity.setMetadata(node.getMetadata());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    /**
     * 转换实体到业务对象
     */
    private ClusterNode convertToClusterNode(ClusterNodeEntity entity) {
        ClusterNode node = new ClusterNode();
        node.setNodeId(entity.getNodeId());
        node.setAddress(entity.getAddress());
        node.setPort(entity.getPort());
        node.setStatus(ClusterManager.NodeStatus.valueOf(entity.getStatus()));
        node.setRegisteredAt(entity.getRegisteredAt().atZone(ZoneId.systemDefault()).toInstant());
        node.setLastHeartbeat(entity.getLastHeartbeat().atZone(ZoneId.systemDefault()).toInstant());
        node.setMetadata(entity.getMetadata());
        return node;
    }

    /**
     * 集群节点实体类
     */
    public static class ClusterNodeEntity {
        private String id;
        private String nodeId;
        private String address;
        private int port;
        private String status;
        private LocalDateTime registeredAt;
        private LocalDateTime lastHeartbeat;
        private String metadata;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getRegisteredAt() { return registeredAt; }
        public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
        
        public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
        
        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * 集群节点数据访问接口
     */
    public interface ClusterNodeDao {
        void save(ClusterNodeEntity entity);
        void update(ClusterNodeEntity entity);
        Optional<ClusterNodeEntity> findByNodeId(String nodeId);
        List<ClusterNodeEntity> findAll();
        List<ClusterNodeEntity> findByStatus(String status);
        void deleteByNodeId(String nodeId);
        int deleteExpiredNodes(LocalDateTime expiredBefore);
    }
}