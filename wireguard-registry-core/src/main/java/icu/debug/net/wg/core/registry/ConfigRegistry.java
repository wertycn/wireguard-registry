package icu.debug.net.wg.core.registry;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;

import java.util.List;
import java.util.Map;

/**
 * 配置注册中心接口
 * 负责管理节点注册、配置分发和变更通知
 */
public interface ConfigRegistry {

    /**
     * 注册网络节点
     */
    void registerNode(String networkId, WireGuardNetworkNode node);

    /**
     * 注销网络节点
     */
    void unregisterNode(String networkId, String nodeId);

    /**
     * 获取网络节点
     */
    WireGuardNetworkNode getNode(String networkId, String nodeId);

    /**
     * 获取网络下的所有节点
     */
    List<WireGuardNetworkNode> getNodes(String networkId);

    /**
     * 生成并分发配置
     */
    void generateAndDistributeConfig(String networkId);

    /**
     * 获取节点配置
     */
    WireGuardIniConfig getNodeConfig(String networkId, String nodeId);

    /**
     * 获取网络的所有配置
     */
    Map<String, WireGuardIniConfig> getNetworkConfigs(String networkId);

    /**
     * 订阅配置变更
     */
    void subscribeConfigChange(String networkId, String nodeId, ConfigChangeListener listener);

    /**
     * 取消订阅配置变更
     */
    void unsubscribeConfigChange(String networkId, String nodeId);

    /**
     * 获取网络版本
     */
    long getNetworkVersion(String networkId);

    /**
     * 检查节点是否在线
     */
    boolean isNodeOnline(String networkId, String nodeId);

    /**
     * 更新节点状态
     */
    void updateNodeStatus(String networkId, String nodeId, boolean online);

    /**
     * 获取所有网络ID
     */
    List<String> getAllNetworkIds();

    /**
     * 删除网络
     */
    void deleteNetwork(String networkId);
}