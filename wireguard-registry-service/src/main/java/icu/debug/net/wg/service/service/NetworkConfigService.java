package icu.debug.net.wg.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import icu.debug.net.wg.core.NetworkNodeWrapper;
import icu.debug.net.wg.core.WireGuardConfigGenerator;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import icu.debug.net.wg.core.storage.ConfigStorage;
import icu.debug.net.wg.core.storage.model.GeneratedConfig;
import icu.debug.net.wg.core.storage.model.StoredNetwork;
import icu.debug.net.wg.service.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 网络配置服务
 *
 * @author WireGuard Registry Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class NetworkConfigService {

    private final ConfigStorage configStorage;
    private final ObjectMapper objectMapper;

    @Autowired
    public NetworkConfigService(ConfigStorage configStorage, ObjectMapper objectMapper) {
        this.configStorage = configStorage;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建网络并生成配置
     */
    @Transactional
    public StoredNetwork createNetwork(String networkId, String name,
                                      WireGuardNetworkStruct struct,
                                      WireGuardNetProperties properties) {
        log.info("Creating network: id={}, name={}", networkId, name);

        // 检查网络是否已存在
        if (configStorage.networkExists(networkId)) {
            throw new IllegalArgumentException("Network already exists: " + networkId);
        }

        // 保存网络配置
        configStorage.saveNetwork(networkId, name, struct, properties);

        // 生成并保存配置
        generateAndSaveConfigs(networkId, struct, properties);

        // 返回保存的网络
        return configStorage.getNetwork(networkId)
            .orElseThrow(() -> new RuntimeException("Failed to create network"));
    }

    /**
     * 更新网络并重新生成配置
     */
    @Transactional
    public StoredNetwork updateNetwork(String networkId,
                                      WireGuardNetworkStruct struct,
                                      WireGuardNetProperties properties) {
        log.info("Updating network: {}", networkId);

        // 验证网络存在
        StoredNetwork network = configStorage.getNetwork(networkId)
            .orElseThrow(() -> new NotFoundException("Network not found: " + networkId));

        // 更新网络配置
        configStorage.saveNetwork(networkId, network.getName(), struct, properties);

        // 递增版本
        long newVersion = configStorage.incrementVersion(networkId);

        // 重新生成配置
        generateAndSaveConfigs(networkId, struct, properties);

        log.info("Network updated: id={}, newVersion={}", networkId, newVersion);

        // 返回更新后的网络
        return configStorage.getNetwork(networkId)
            .orElseThrow(() -> new RuntimeException("Failed to update network"));
    }

    /**
     * 获取网络配置
     */
    public StoredNetwork getNetwork(String networkId) {
        return configStorage.getNetwork(networkId)
            .orElseThrow(() -> new NotFoundException("Network not found: " + networkId));
    }

    /**
     * 获取所有网络列表
     */
    public List<StoredNetwork> listNetworks() {
        return configStorage.listNetworks();
    }

    /**
     * 删除网络
     */
    @Transactional
    public void deleteNetwork(String networkId) {
        log.info("Deleting network: {}", networkId);

        if (!configStorage.networkExists(networkId)) {
            throw new NotFoundException("Network not found: " + networkId);
        }

        configStorage.deleteNetwork(networkId);
    }

    /**
     * 获取节点配置
     */
    public GeneratedConfig getNodeConfig(String networkId, String nodeId) {
        return configStorage.getNodeConfig(networkId, nodeId)
            .orElseThrow(() -> new NotFoundException(
                String.format("Config not found: network=%s, node=%s", networkId, nodeId)));
    }

    /**
     * 获取网络所有配置
     */
    public Map<String, GeneratedConfig> getNetworkConfigs(String networkId) {
        if (!configStorage.networkExists(networkId)) {
            throw new NotFoundException("Network not found: " + networkId);
        }

        return configStorage.getNetworkConfigs(networkId);
    }

    /**
     * 重新生成配置
     */
    @Transactional
    public StoredNetwork regenerateConfigs(String networkId) {
        log.info("Regenerating configs for network: {}", networkId);

        StoredNetwork network = configStorage.getNetwork(networkId)
            .orElseThrow(() -> new NotFoundException("Network not found: " + networkId));

        // 递增版本
        long newVersion = configStorage.incrementVersion(networkId);

        // 重新生成配置
        generateAndSaveConfigs(networkId, network.getStruct(), network.getProperties());

        log.info("Configs regenerated: id={}, newVersion={}", networkId, newVersion);

        return configStorage.getNetwork(networkId)
            .orElseThrow(() -> new RuntimeException("Failed to regenerate configs"));
    }

    /**
     * 生成并保存配置（内部方法）
     */
    private void generateAndSaveConfigs(String networkId,
                                       WireGuardNetworkStruct struct,
                                       WireGuardNetProperties properties) {
        log.debug("Generating configs for network: {}", networkId);

        try {
            // 使用 WireGuardConfigGenerator 生成配置
            WireGuardConfigGenerator generator = new WireGuardConfigGenerator(struct, properties);
            Map<String, NetworkNodeWrapper> allNodes = generator.getAllNodeWrapperMap();

            Map<String, WireGuardIniConfig> configs = new HashMap<>();
            for (Map.Entry<String, NetworkNodeWrapper> entry : allNodes.entrySet()) {
                String nodeId = entry.getKey();
                NetworkNodeWrapper wrapper = entry.getValue();
                WireGuardIniConfig config = generator.generateNodeConfig(wrapper.getNetworkNode());
                configs.put(nodeId, config);
            }

            // 保存配置
            long version = configStorage.getNetworkVersion(networkId);
            configStorage.saveNodeConfigs(networkId, configs, version);

            log.info("Generated and saved {} configs for network: {}", configs.size(), networkId);

        } catch (Exception e) {
            log.error("Failed to generate configs for network: {}", networkId, e);
            throw new RuntimeException("Failed to generate configs", e);
        }
    }
}
