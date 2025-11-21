package icu.debug.net.wg.storage.sqlite;

import com.fasterxml.jackson.databind.ObjectMapper;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import icu.debug.net.wg.core.storage.ConfigStorage;
import icu.debug.net.wg.core.storage.model.GeneratedConfig;
import icu.debug.net.wg.core.storage.model.StoredNetwork;
import icu.debug.net.wg.storage.sqlite.entity.ConfigEntity;
import icu.debug.net.wg.storage.sqlite.entity.NetworkEntity;
import icu.debug.net.wg.storage.sqlite.repository.ConfigRepository;
import icu.debug.net.wg.storage.sqlite.repository.NetworkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SqliteConfigStorage implements ConfigStorage {

    private final NetworkRepository networkRepository;
    private final ConfigRepository configRepository;
    private final ObjectMapper objectMapper;

    public SqliteConfigStorage(NetworkRepository networkRepository,
                              ConfigRepository configRepository,
                              ObjectMapper objectMapper) {
        this.networkRepository = networkRepository;
        this.configRepository = configRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void saveNetwork(String networkId, String name,
                           WireGuardNetworkStruct struct,
                           WireGuardNetProperties properties) {
        log.debug("Saving network to SQLite: {}", networkId);

        try {
            NetworkEntity entity = networkRepository.findById(networkId)
                .orElse(new NetworkEntity());

            entity.setId(networkId);
            entity.setName(name);
            entity.setStructJson(objectMapper.writeValueAsString(struct));
            entity.setPropertiesJson(objectMapper.writeValueAsString(properties));

            if (entity.getCreatedAt() == null) {
                entity.setVersion(1L);
            }

            networkRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save network", e);
        }
    }

    @Override
    public Optional<StoredNetwork> getNetwork(String networkId) {
        log.debug("Getting network from SQLite: {}", networkId);

        return networkRepository.findById(networkId)
            .map(this::toStoredNetwork);
    }

    @Override
    public List<StoredNetwork> listNetworks() {
        log.debug("Listing all networks from SQLite");

        return networkRepository.findAll().stream()
            .map(this::toStoredNetwork)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNetwork(String networkId) {
        log.debug("Deleting network from SQLite: {}", networkId);

        configRepository.deleteByNetworkId(networkId);
        networkRepository.deleteById(networkId);
    }

    @Override
    public boolean networkExists(String networkId) {
        return networkRepository.existsById(networkId);
    }

    @Override
    @Transactional
    public void saveNodeConfig(String networkId, String nodeId,
                               String nodeName, WireGuardIniConfig config, long version) {
        log.debug("Saving node config to SQLite: network={}, node={}", networkId, nodeId);

        try {
            ConfigEntity entity = configRepository
                .findByNetworkIdAndNodeId(networkId, nodeId)
                .orElse(new ConfigEntity());

            entity.setNetworkId(networkId);
            entity.setNodeId(nodeId);
            entity.setNodeName(nodeName);
            entity.setConfigJson(objectMapper.writeValueAsString(config));
            entity.setConfigText(config.toIniString());
            entity.setVersion(version);

            configRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save node config", e);
        }
    }

    @Override
    @Transactional
    public void saveNodeConfigs(String networkId, Map<String, WireGuardIniConfig> configsMap,
                                long version) {
        log.debug("Batch saving node configs to SQLite: network={}, count={}",
                 networkId, configsMap.size());

        for (Map.Entry<String, WireGuardIniConfig> entry : configsMap.entrySet()) {
            saveNodeConfig(networkId, entry.getKey(), entry.getKey(), entry.getValue(), version);
        }
    }

    @Override
    public Optional<GeneratedConfig> getNodeConfig(String networkId, String nodeId) {
        log.debug("Getting node config from SQLite: network={}, node={}", networkId, nodeId);

        return configRepository.findByNetworkIdAndNodeId(networkId, nodeId)
            .map(this::toGeneratedConfig);
    }

    @Override
    public Map<String, GeneratedConfig> getNetworkConfigs(String networkId) {
        log.debug("Getting all configs for network from SQLite: {}", networkId);

        return configRepository.findByNetworkId(networkId).stream()
            .map(this::toGeneratedConfig)
            .collect(Collectors.toMap(GeneratedConfig::getNodeId, c -> c));
    }

    @Override
    @Transactional
    public void deleteNodeConfig(String networkId, String nodeId) {
        log.debug("Deleting node config from SQLite: network={}, node={}", networkId, nodeId);

        configRepository.deleteByNetworkIdAndNodeId(networkId, nodeId);
    }

    @Override
    public long getNetworkVersion(String networkId) {
        return networkRepository.findById(networkId)
            .map(NetworkEntity::getVersion)
            .orElse(1L);
    }

    @Override
    @Transactional
    public long incrementVersion(String networkId) {
        NetworkEntity entity = networkRepository.findById(networkId)
            .orElseThrow(() -> new IllegalArgumentException("Network not found: " + networkId));

        long newVersion = entity.getVersion() + 1;
        entity.setVersion(newVersion);
        networkRepository.save(entity);

        log.debug("Incremented version for network {}: {}", networkId, newVersion);
        return newVersion;
    }

    private StoredNetwork toStoredNetwork(NetworkEntity entity) {
        try {
            StoredNetwork network = new StoredNetwork();
            network.setId(entity.getId());
            network.setName(entity.getName());
            network.setStruct(objectMapper.readValue(
                entity.getStructJson(), WireGuardNetworkStruct.class));
            network.setProperties(objectMapper.readValue(
                entity.getPropertiesJson(), WireGuardNetProperties.class));
            network.setVersion(entity.getVersion());
            network.setCreatedAt(entity.getCreatedAt());
            network.setUpdatedAt(entity.getUpdatedAt());
            return network;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert network entity", e);
        }
    }

    private GeneratedConfig toGeneratedConfig(ConfigEntity entity) {
        try {
            GeneratedConfig config = new GeneratedConfig();
            config.setNetworkId(entity.getNetworkId());
            config.setNodeId(entity.getNodeId());
            config.setNodeName(entity.getNodeName());
            config.setConfig(objectMapper.readValue(
                entity.getConfigJson(), WireGuardIniConfig.class));
            config.setConfigText(entity.getConfigText());
            config.setVersion(entity.getVersion());
            config.setGeneratedAt(entity.getGeneratedAt());
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert config entity", e);
        }
    }
}
