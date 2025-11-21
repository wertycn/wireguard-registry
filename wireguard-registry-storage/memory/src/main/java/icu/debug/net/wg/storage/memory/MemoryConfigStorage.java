package icu.debug.net.wg.storage.memory;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import icu.debug.net.wg.core.storage.ConfigStorage;
import icu.debug.net.wg.core.storage.model.GeneratedConfig;
import icu.debug.net.wg.core.storage.model.StoredNetwork;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存存储实现 - 基于 ConcurrentHashMap
 *
 * <p>适用于开发测试环境，数据不会持久化到磁盘
 *
 * @author WireGuard Registry Team
 * @since 1.0.0
 */
@Slf4j
public class MemoryConfigStorage implements ConfigStorage {

    private final Map<String, StoredNetwork> networks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, GeneratedConfig>> configs = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> versions = new ConcurrentHashMap<>();

    @Override
    public void saveNetwork(String networkId, String name,
                           WireGuardNetworkStruct struct,
                           WireGuardNetProperties properties) {
        log.debug("Saving network to memory: {}", networkId);

        StoredNetwork network = new StoredNetwork();
        network.setId(networkId);
        network.setName(name);
        network.setStruct(struct);
        network.setProperties(properties);
        network.setVersion(getNetworkVersion(networkId));
        network.setUpdatedAt(LocalDateTime.now());

        if (networks.containsKey(networkId)) {
            network.setCreatedAt(networks.get(networkId).getCreatedAt());
        } else {
            network.setCreatedAt(LocalDateTime.now());
            versions.putIfAbsent(networkId, new AtomicLong(1));
        }

        networks.put(networkId, network);
    }

    @Override
    public Optional<StoredNetwork> getNetwork(String networkId) {
        log.debug("Getting network from memory: {}", networkId);
        return Optional.ofNullable(networks.get(networkId));
    }

    @Override
    public List<StoredNetwork> listNetworks() {
        log.debug("Listing all networks from memory, total: {}", networks.size());
        return new ArrayList<>(networks.values());
    }

    @Override
    public void deleteNetwork(String networkId) {
        log.debug("Deleting network from memory: {}", networkId);
        networks.remove(networkId);
        configs.remove(networkId);
        versions.remove(networkId);
    }

    @Override
    public boolean networkExists(String networkId) {
        return networks.containsKey(networkId);
    }

    @Override
    public void saveNodeConfig(String networkId, String nodeId,
                               String nodeName, WireGuardIniConfig config, long version) {
        log.debug("Saving node config to memory: network={}, node={}", networkId, nodeId);

        GeneratedConfig generatedConfig = new GeneratedConfig();
        generatedConfig.setNetworkId(networkId);
        generatedConfig.setNodeId(nodeId);
        generatedConfig.setNodeName(nodeName);
        generatedConfig.setConfig(config);
        generatedConfig.setConfigText(config.toIniString());
        generatedConfig.setVersion(version);
        generatedConfig.setGeneratedAt(LocalDateTime.now());

        configs.computeIfAbsent(networkId, k -> new ConcurrentHashMap<>())
               .put(nodeId, generatedConfig);
    }

    @Override
    public void saveNodeConfigs(String networkId, Map<String, WireGuardIniConfig> configsMap,
                                long version) {
        log.debug("Batch saving node configs to memory: network={}, count={}", networkId, configsMap.size());

        for (Map.Entry<String, WireGuardIniConfig> entry : configsMap.entrySet()) {
            String nodeId = entry.getKey();
            WireGuardIniConfig config = entry.getValue();
            String nodeName = nodeId;

            saveNodeConfig(networkId, nodeId, nodeName, config, version);
        }
    }

    @Override
    public Optional<GeneratedConfig> getNodeConfig(String networkId, String nodeId) {
        log.debug("Getting node config from memory: network={}, node={}", networkId, nodeId);

        Map<String, GeneratedConfig> networkConfigs = configs.get(networkId);
        if (networkConfigs == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(networkConfigs.get(nodeId));
    }

    @Override
    public Map<String, GeneratedConfig> getNetworkConfigs(String networkId) {
        log.debug("Getting all configs for network from memory: {}", networkId);

        Map<String, GeneratedConfig> networkConfigs = configs.get(networkId);
        if (networkConfigs == null) {
            return Collections.emptyMap();
        }

        return new HashMap<>(networkConfigs);
    }

    @Override
    public void deleteNodeConfig(String networkId, String nodeId) {
        log.debug("Deleting node config from memory: network={}, node={}", networkId, nodeId);

        Map<String, GeneratedConfig> networkConfigs = configs.get(networkId);
        if (networkConfigs != null) {
            networkConfigs.remove(nodeId);
        }
    }

    @Override
    public long getNetworkVersion(String networkId) {
        AtomicLong version = versions.get(networkId);
        return version != null ? version.get() : 1L;
    }

    @Override
    public long incrementVersion(String networkId) {
        AtomicLong version = versions.computeIfAbsent(networkId, k -> new AtomicLong(1));
        long newVersion = version.incrementAndGet();

        StoredNetwork network = networks.get(networkId);
        if (network != null) {
            network.setVersion(newVersion);
            network.setUpdatedAt(LocalDateTime.now());
        }

        log.debug("Incremented version for network {}: {}", networkId, newVersion);
        return newVersion;
    }
}
