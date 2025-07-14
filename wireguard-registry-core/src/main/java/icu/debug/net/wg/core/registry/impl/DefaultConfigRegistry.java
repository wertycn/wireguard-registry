package icu.debug.net.wg.core.registry.impl;

import icu.debug.net.wg.core.WireGuardConfigGenerator;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import icu.debug.net.wg.core.registry.ConfigChangeListener;
import icu.debug.net.wg.core.registry.ConfigRegistry;
import icu.debug.net.wg.core.storage.ConfigStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 默认配置注册中心实现
 */
@Slf4j
public class DefaultConfigRegistry implements ConfigRegistry {

    private final ConfigStorage configStorage;
    private final WireGuardConfigGenerator configGenerator;
    private final Map<String, Map<String, ConfigChangeListener>> listeners = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Long>> nodeLastHeartbeat = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // 心跳超时时间（秒）
    private static final long HEARTBEAT_TIMEOUT = 30;

    public DefaultConfigRegistry(ConfigStorage configStorage, WireGuardConfigGenerator configGenerator) {
        this.configStorage = configStorage;
        this.configGenerator = configGenerator;
        
        // 启动心跳检查定时任务
        startHeartbeatCheck();
    }

    @Override
    public void registerNode(String networkId, WireGuardNetworkNode node) {
        log.info("Registering node {} in network {}", node.getServerNode().getHostname(), networkId);
        
        // 获取旧配置
        String nodeId = node.getServerNode().getHostname();
        WireGuardIniConfig oldConfig = configStorage.getGeneratedConfig(networkId, nodeId).orElse(null);
        
        // 保存节点
        configStorage.saveNetworkNode(networkId, node);
        
        // 更新心跳时间
        updateNodeHeartbeat(networkId, nodeId);
        
        // 重新生成配置
        generateAndDistributeConfig(networkId);
        
        // 通知配置变更
        WireGuardIniConfig newConfig = configStorage.getGeneratedConfig(networkId, nodeId).orElse(null);
        notifyConfigChange(networkId, nodeId, oldConfig, newConfig);
    }

    @Override
    public void unregisterNode(String networkId, String nodeId) {
        log.info("Unregistering node {} from network {}", nodeId, networkId);
        
        // 删除节点
        configStorage.deleteNetworkNode(networkId, nodeId);
        configStorage.deleteGeneratedConfig(networkId, nodeId);
        
        // 移除心跳记录
        removeNodeHeartbeat(networkId, nodeId);
        
        // 重新生成配置
        generateAndDistributeConfig(networkId);
        
        // 通知节点下线
        notifyNodeOffline(networkId, nodeId);
        
        // 移除监听器
        unsubscribeConfigChange(networkId, nodeId);
    }

    @Override
    public WireGuardNetworkNode getNode(String networkId, String nodeId) {
        return configStorage.getNetworkNode(networkId, nodeId).orElse(null);
    }

    @Override
    public List<WireGuardNetworkNode> getNodes(String networkId) {
        return configStorage.getNetworkNodes(networkId);
    }

    @Override
    public void generateAndDistributeConfig(String networkId) {
        log.info("Generating and distributing config for network {}", networkId);
        
        try {
            List<WireGuardNetworkNode> nodes = configStorage.getNetworkNodes(networkId);
            if (nodes.isEmpty()) {
                log.warn("No nodes found in network {}", networkId);
                return;
            }
            
            // 创建网络结构
            WireGuardNetworkStruct networkStruct = new WireGuardNetworkStruct();
            // TODO: 需要正确设置 networkStruct 的 localAreaNetworks
            
            // 生成配置
            WireGuardConfigGenerator generator = new WireGuardConfigGenerator(networkStruct, null);
            Map<String, WireGuardIniConfig> configs = generator.buildWireGuardIniConfigMap();
            
            // 保存配置
            for (Map.Entry<String, WireGuardIniConfig> entry : configs.entrySet()) {
                configStorage.saveGeneratedConfig(networkId, entry.getKey(), entry.getValue());
            }
            
            log.info("Generated and saved {} configs for network {}", configs.size(), networkId);
            
        } catch (Exception e) {
            log.error("Failed to generate config for network {}", networkId, e);
            throw new RuntimeException("Failed to generate config", e);
        }
    }

    @Override
    public WireGuardIniConfig getNodeConfig(String networkId, String nodeId) {
        return configStorage.getGeneratedConfig(networkId, nodeId).orElse(null);
    }

    @Override
    public Map<String, WireGuardIniConfig> getNetworkConfigs(String networkId) {
        return configStorage.getNetworkConfigs(networkId);
    }

    @Override
    public void subscribeConfigChange(String networkId, String nodeId, ConfigChangeListener listener) {
        listeners.computeIfAbsent(networkId, k -> new ConcurrentHashMap<>())
                .put(nodeId, listener);
        log.info("Subscribed config change for node {} in network {}", nodeId, networkId);
    }

    @Override
    public void unsubscribeConfigChange(String networkId, String nodeId) {
        Optional.ofNullable(listeners.get(networkId))
                .ifPresent(networkListeners -> networkListeners.remove(nodeId));
        log.info("Unsubscribed config change for node {} in network {}", nodeId, networkId);
    }

    @Override
    public long getNetworkVersion(String networkId) {
        return configStorage.getNetworkVersion(networkId);
    }

    @Override
    public boolean isNodeOnline(String networkId, String nodeId) {
        return Optional.ofNullable(nodeLastHeartbeat.get(networkId))
                .map(networkHeartbeats -> networkHeartbeats.get(nodeId))
                .map(lastHeartbeat -> System.currentTimeMillis() - lastHeartbeat < HEARTBEAT_TIMEOUT * 1000)
                .orElse(false);
    }

    @Override
    public void updateNodeStatus(String networkId, String nodeId, boolean online) {
        if (online) {
            updateNodeHeartbeat(networkId, nodeId);
        } else {
            removeNodeHeartbeat(networkId, nodeId);
            notifyNodeOffline(networkId, nodeId);
        }
    }

    @Override
    public List<String> getAllNetworkIds() {
        return configStorage.getAllNetworkIds();
    }

    @Override
    public void deleteNetwork(String networkId) {
        log.info("Deleting network {}", networkId);
        
        // 通知网络删除
        notifyNetworkDeleted(networkId);
        
        // 删除存储数据
        configStorage.deleteNetwork(networkId);
        
        // 清理内存数据
        listeners.remove(networkId);
        nodeLastHeartbeat.remove(networkId);
    }

    private void updateNodeHeartbeat(String networkId, String nodeId) {
        nodeLastHeartbeat.computeIfAbsent(networkId, k -> new ConcurrentHashMap<>())
                .put(nodeId, System.currentTimeMillis());
    }

    private void removeNodeHeartbeat(String networkId, String nodeId) {
        Optional.ofNullable(nodeLastHeartbeat.get(networkId))
                .ifPresent(networkHeartbeats -> networkHeartbeats.remove(nodeId));
    }

    private void startHeartbeatCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkOfflineNodes();
            } catch (Exception e) {
                log.error("Error checking offline nodes", e);
            }
        }, HEARTBEAT_TIMEOUT, HEARTBEAT_TIMEOUT, TimeUnit.SECONDS);
    }

    private void checkOfflineNodes() {
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<String, Map<String, Long>> networkEntry : nodeLastHeartbeat.entrySet()) {
            String networkId = networkEntry.getKey();
            Map<String, Long> networkHeartbeats = networkEntry.getValue();
            
            List<String> offlineNodes = new ArrayList<>();
            
            for (Map.Entry<String, Long> nodeEntry : networkHeartbeats.entrySet()) {
                String nodeId = nodeEntry.getKey();
                long lastHeartbeat = nodeEntry.getValue();
                
                if (currentTime - lastHeartbeat > HEARTBEAT_TIMEOUT * 1000) {
                    offlineNodes.add(nodeId);
                }
            }
            
            for (String nodeId : offlineNodes) {
                log.warn("Node {} in network {} is offline", nodeId, networkId);
                notifyNodeOffline(networkId, nodeId);
                networkHeartbeats.remove(nodeId);
            }
        }
    }

    private void notifyConfigChange(String networkId, String nodeId, WireGuardIniConfig oldConfig, WireGuardIniConfig newConfig) {
        Optional.ofNullable(listeners.get(networkId))
                .map(networkListeners -> networkListeners.get(nodeId))
                .ifPresent(listener -> {
                    try {
                        long version = getNetworkVersion(networkId);
                        listener.onConfigChanged(networkId, nodeId, oldConfig, newConfig, version);
                    } catch (Exception e) {
                        log.error("Error notifying config change for node {} in network {}", nodeId, networkId, e);
                    }
                });
    }

    private void notifyNodeOffline(String networkId, String nodeId) {
        Optional.ofNullable(listeners.get(networkId))
                .map(networkListeners -> networkListeners.get(nodeId))
                .ifPresent(listener -> {
                    try {
                        listener.onNodeOffline(networkId, nodeId);
                    } catch (Exception e) {
                        log.error("Error notifying node offline for node {} in network {}", nodeId, networkId, e);
                    }
                });
    }

    private void notifyNetworkDeleted(String networkId) {
        Optional.ofNullable(listeners.get(networkId))
                .ifPresent(networkListeners -> {
                    for (ConfigChangeListener listener : networkListeners.values()) {
                        try {
                            listener.onNetworkDeleted(networkId);
                        } catch (Exception e) {
                            log.error("Error notifying network deleted for network {}", networkId, e);
                        }
                    }
                });
    }
}