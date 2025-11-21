package icu.debug.net.wg.core.storage;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import icu.debug.net.wg.core.storage.model.GeneratedConfig;
import icu.debug.net.wg.core.storage.model.StoredNetwork;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置存储接口 - 完全独立的接口定义，不依赖任何 ORM 框架
 *
 * <p>该接口定义了 WireGuard 配置的存储和管理能力，支持多种存储后端实现。
 * 每个实现可以选择使用适合自己的存储技术（如 JPA、MongoDB、JDBC 等）。
 *
 * @author WireGuard Registry Team
 * @since 1.0.0
 */
public interface ConfigStorage {

    // ========== 网络管理 ==========

    /**
     * 保存网络配置
     *
     * @param networkId 网络唯一标识
     * @param name 网络名称
     * @param struct 网络拓扑结构
     * @param properties 配置参数
     */
    void saveNetwork(String networkId, String name,
                    WireGuardNetworkStruct struct,
                    WireGuardNetProperties properties);

    /**
     * 获取网络配置
     *
     * @param networkId 网络唯一标识
     * @return 网络配置，如果不存在则返回 Optional.empty()
     */
    Optional<StoredNetwork> getNetwork(String networkId);

    /**
     * 获取所有网络列表
     *
     * @return 所有网络配置列表
     */
    List<StoredNetwork> listNetworks();

    /**
     * 删除网络（级联删除所有相关配置）
     *
     * @param networkId 网络唯一标识
     */
    void deleteNetwork(String networkId);

    /**
     * 检查网络是否存在
     *
     * @param networkId 网络唯一标识
     * @return 如果存在返回 true，否则返回 false
     */
    boolean networkExists(String networkId);

    // ========== 配置管理 ==========

    /**
     * 保存单个节点配置
     *
     * @param networkId 网络ID
     * @param nodeId 节点ID
     * @param nodeName 节点名称
     * @param config 配置对象
     * @param version 配置版本
     */
    void saveNodeConfig(String networkId, String nodeId,
                       String nodeName, WireGuardIniConfig config, long version);

    /**
     * 批量保存节点配置
     *
     * @param networkId 网络ID
     * @param configs 节点配置映射 (nodeId -> config)
     * @param version 配置版本
     */
    void saveNodeConfigs(String networkId, Map<String, WireGuardIniConfig> configs,
                        long version);

    /**
     * 获取单个节点配置
     *
     * @param networkId 网络ID
     * @param nodeId 节点ID
     * @return 节点配置，如果不存在则返回 Optional.empty()
     */
    Optional<GeneratedConfig> getNodeConfig(String networkId, String nodeId);

    /**
     * 获取网络的所有配置
     *
     * @param networkId 网络ID
     * @return 节点配置映射 (nodeId -> GeneratedConfig)
     */
    Map<String, GeneratedConfig> getNetworkConfigs(String networkId);

    /**
     * 删除节点配置
     *
     * @param networkId 网络ID
     * @param nodeId 节点ID
     */
    void deleteNodeConfig(String networkId, String nodeId);

    // ========== 版本管理 ==========

    /**
     * 获取网络当前版本
     *
     * @param networkId 网络ID
     * @return 当前版本号，如果网络不存在则返回 0
     */
    long getNetworkVersion(String networkId);

    /**
     * 递增版本号并返回新版本
     *
     * @param networkId 网络ID
     * @return 新的版本号
     */
    long incrementVersion(String networkId);
}
