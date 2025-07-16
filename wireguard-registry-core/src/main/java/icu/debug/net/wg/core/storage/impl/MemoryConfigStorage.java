package icu.debug.net.wg.core.storage.impl;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import icu.debug.net.wg.core.storage.ConfigStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存配置存储实现
 * 用于测试和开发环境
 */
public class MemoryConfigStorage implements ConfigStorage {

    private final Map<String, Map<String, WireGuardNetworkNode>> networkNodes = new ConcurrentHashMap<>();
    private final Map<String, Map<String, WireGuardIniConfig>> generatedConfigs = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> networkVersions = new ConcurrentHashMap<>();

    @Override
    public void saveNetworkNode(String networkId, WireGuardNetworkNode node) {
        networkNodes.computeIfAbsent(networkId, k -> new ConcurrentHashMap<>())
                .put(node.getServerNode().getHostname(), node);
        updateNetworkVersion(networkId);
    }

    @Override
    public Optional<WireGuardNetworkNode> getNetworkNode(String networkId, String nodeId) {
        return Optional.ofNullable(networkNodes.get(networkId))
                .map(nodes -> nodes.get(nodeId));
    }

    @Override
    public List<WireGuardNetworkNode> getNetworkNodes(String networkId) {
        return Optional.ofNullable(networkNodes.get(networkId))
                .map(nodes -> new ArrayList<>(nodes.values()))
                .orElse(new ArrayList<>());
    }

    @Override
    public void deleteNetworkNode(String networkId, String nodeId) {
        Optional.ofNullable(networkNodes.get(networkId))
                .ifPresent(nodes -> nodes.remove(nodeId));
        updateNetworkVersion(networkId);
    }

    @Override
    public void saveGeneratedConfig(String networkId, String nodeId, WireGuardIniConfig config) {
        generatedConfigs.computeIfAbsent(networkId, k -> new ConcurrentHashMap<>())
                .put(nodeId, config);
    }

    @Override
    public Optional<WireGuardIniConfig> getGeneratedConfig(String networkId, String nodeId) {
        return Optional.ofNullable(generatedConfigs.get(networkId))
                .map(configs -> configs.get(nodeId));
    }

    @Override
    public Map<String, WireGuardIniConfig> getNetworkConfigs(String networkId) {
        return Optional.ofNullable(generatedConfigs.get(networkId))
                .map(HashMap::new)
                .orElse(new HashMap<>());
    }

    @Override
    public void deleteGeneratedConfig(String networkId, String nodeId) {
        Optional.ofNullable(generatedConfigs.get(networkId))
                .ifPresent(configs -> configs.remove(nodeId));
    }

    @Override
    public List<String> getAllNetworkIds() {
        // 合并有节点的网络和只有版本记录的网络
        Set<String> allNetworkIds = new HashSet<>();
        allNetworkIds.addAll(networkNodes.keySet());
        allNetworkIds.addAll(networkVersions.keySet());
        return new ArrayList<>(allNetworkIds);
    }

    @Override
    public void deleteNetwork(String networkId) {
        networkNodes.remove(networkId);
        generatedConfigs.remove(networkId);
        networkVersions.remove(networkId);
    }

    @Override
    public boolean networkExists(String networkId) {
        // 检查是否有节点记录或版本记录
        return networkNodes.containsKey(networkId) || networkVersions.containsKey(networkId);
    }

    @Override
    public long getNetworkVersion(String networkId) {
        return networkVersions.computeIfAbsent(networkId, k -> new AtomicLong(0)).get();
    }

    @Override
    public void updateNetworkVersion(String networkId) {
        networkVersions.computeIfAbsent(networkId, k -> new AtomicLong(0)).incrementAndGet();
    }
}