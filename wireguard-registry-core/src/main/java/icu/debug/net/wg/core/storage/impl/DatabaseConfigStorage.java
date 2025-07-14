package icu.debug.net.wg.core.storage.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import icu.debug.net.wg.core.storage.ConfigStorage;
import icu.debug.net.wg.core.storage.dao.GeneratedConfigDao;
import icu.debug.net.wg.core.storage.dao.NetworkNodeDao;
import icu.debug.net.wg.core.storage.dao.NetworkVersionDao;
import icu.debug.net.wg.core.storage.entity.GeneratedConfigEntity;
import icu.debug.net.wg.core.storage.entity.NetworkNodeEntity;
import icu.debug.net.wg.core.storage.entity.NetworkVersionEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库配置存储实现
 * 支持MySQL、MongoDB、SQLite等多种数据库
 */
@Slf4j
public class DatabaseConfigStorage implements ConfigStorage {

    private final NetworkNodeDao networkNodeDao;
    private final GeneratedConfigDao generatedConfigDao;
    private final NetworkVersionDao networkVersionDao;
    private final ObjectMapper objectMapper;

    public DatabaseConfigStorage(NetworkNodeDao networkNodeDao, 
                                GeneratedConfigDao generatedConfigDao,
                                NetworkVersionDao networkVersionDao,
                                ObjectMapper objectMapper) {
        this.networkNodeDao = networkNodeDao;
        this.generatedConfigDao = generatedConfigDao;
        this.networkVersionDao = networkVersionDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveNetworkNode(String networkId, WireGuardNetworkNode node) {
        try {
            String nodeData = objectMapper.writeValueAsString(node);
            String nodeId = node.getServerNode().getHostname();
            
            Optional<NetworkNodeEntity> existing = networkNodeDao.findByNetworkIdAndNodeId(networkId, nodeId);
            if (existing.isPresent()) {
                NetworkNodeEntity entity = existing.get();
                entity.setNodeData(nodeData);
                entity.setUpdatedAt(java.time.LocalDateTime.now());
                networkNodeDao.save(entity);
            } else {
                NetworkNodeEntity entity = new NetworkNodeEntity(networkId, nodeId, nodeData);
                networkNodeDao.save(entity);
            }
            
            updateNetworkVersion(networkId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize network node", e);
            throw new RuntimeException("Failed to save network node", e);
        }
    }

    @Override
    public Optional<WireGuardNetworkNode> getNetworkNode(String networkId, String nodeId) {
        return networkNodeDao.findByNetworkIdAndNodeId(networkId, nodeId)
                .map(entity -> {
                    try {
                        return objectMapper.readValue(entity.getNodeData(), WireGuardNetworkNode.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize network node", e);
                        return null;
                    }
                });
    }

    @Override
    public List<WireGuardNetworkNode> getNetworkNodes(String networkId) {
        return networkNodeDao.findByNetworkId(networkId)
                .stream()
                .map(entity -> {
                    try {
                        return objectMapper.readValue(entity.getNodeData(), WireGuardNetworkNode.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize network node", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteNetworkNode(String networkId, String nodeId) {
        networkNodeDao.deleteByNetworkIdAndNodeId(networkId, nodeId);
        updateNetworkVersion(networkId);
    }

    @Override
    public void saveGeneratedConfig(String networkId, String nodeId, WireGuardIniConfig config) {
        try {
            String configData = objectMapper.writeValueAsString(config);
            
            Optional<GeneratedConfigEntity> existing = generatedConfigDao.findByNetworkIdAndNodeId(networkId, nodeId);
            if (existing.isPresent()) {
                GeneratedConfigEntity entity = existing.get();
                entity.setConfigData(configData);
                entity.setUpdatedAt(java.time.LocalDateTime.now());
                generatedConfigDao.save(entity);
            } else {
                GeneratedConfigEntity entity = new GeneratedConfigEntity(networkId, nodeId, configData);
                generatedConfigDao.save(entity);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize generated config", e);
            throw new RuntimeException("Failed to save generated config", e);
        }
    }

    @Override
    public Optional<WireGuardIniConfig> getGeneratedConfig(String networkId, String nodeId) {
        return generatedConfigDao.findByNetworkIdAndNodeId(networkId, nodeId)
                .map(entity -> {
                    try {
                        return objectMapper.readValue(entity.getConfigData(), WireGuardIniConfig.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize generated config", e);
                        return null;
                    }
                });
    }

    @Override
    public Map<String, WireGuardIniConfig> getNetworkConfigs(String networkId) {
        return generatedConfigDao.findByNetworkId(networkId)
                .stream()
                .collect(Collectors.toMap(
                        GeneratedConfigEntity::getNodeId,
                        entity -> {
                            try {
                                return objectMapper.readValue(entity.getConfigData(), WireGuardIniConfig.class);
                            } catch (JsonProcessingException e) {
                                log.error("Failed to deserialize generated config", e);
                                return null;
                            }
                        }
                ))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void deleteGeneratedConfig(String networkId, String nodeId) {
        generatedConfigDao.deleteByNetworkIdAndNodeId(networkId, nodeId);
    }

    @Override
    public List<String> getAllNetworkIds() {
        return networkNodeDao.findAllNetworkIds();
    }

    @Override
    public void deleteNetwork(String networkId) {
        networkNodeDao.deleteByNetworkId(networkId);
        generatedConfigDao.deleteByNetworkId(networkId);
        networkVersionDao.deleteByNetworkId(networkId);
    }

    @Override
    public boolean networkExists(String networkId) {
        return networkNodeDao.existsByNetworkId(networkId);
    }

    @Override
    public long getNetworkVersion(String networkId) {
        return networkVersionDao.findByNetworkId(networkId)
                .map(NetworkVersionEntity::getVersion)
                .orElse(0L);
    }

    @Override
    public void updateNetworkVersion(String networkId) {
        Optional<NetworkVersionEntity> existing = networkVersionDao.findByNetworkId(networkId);
        if (existing.isPresent()) {
            networkVersionDao.incrementVersion(networkId);
        } else {
            NetworkVersionEntity entity = new NetworkVersionEntity(networkId);
            networkVersionDao.save(entity);
        }
    }
}