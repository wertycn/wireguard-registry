package icu.debug.net.wg.core.storage;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置存储接口
 * 支持多种存储后端：MySQL, MongoDB, SQLite
 */
public interface ConfigStorage {

    /**
     * 保存网络节点配置
     */
    void saveNetworkNode(String networkId, WireGuardNetworkNode node);

    /**
     * 获取网络节点配置
     */
    Optional<WireGuardNetworkNode> getNetworkNode(String networkId, String nodeId);

    /**
     * 获取网络下的所有节点
     */
    List<WireGuardNetworkNode> getNetworkNodes(String networkId);

    /**
     * 删除网络节点
     */
    void deleteNetworkNode(String networkId, String nodeId);

    /**
     * 保存生成的配置
     */
    void saveGeneratedConfig(String networkId, String nodeId, WireGuardIniConfig config);

    /**
     * 获取生成的配置
     */
    Optional<WireGuardIniConfig> getGeneratedConfig(String networkId, String nodeId);

    /**
     * 获取网络的所有配置
     */
    Map<String, WireGuardIniConfig> getNetworkConfigs(String networkId);

    /**
     * 删除生成的配置
     */
    void deleteGeneratedConfig(String networkId, String nodeId);

    /**
     * 获取所有网络ID
     */
    List<String> getAllNetworkIds();

    /**
     * 删除整个网络
     */
    void deleteNetwork(String networkId);

    /**
     * 检查网络是否存在
     */
    boolean networkExists(String networkId);

    /**
     * 获取网络的配置版本号
     */
    long getNetworkVersion(String networkId);

    /**
     * 更新网络版本号
     */
    void updateNetworkVersion(String networkId);
}