package icu.debug.net.wg.core.registry;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;

/**
 * 配置变更监听器接口
 */
public interface ConfigChangeListener {

    /**
     * 配置变更事件
     * 
     * @param networkId 网络ID
     * @param nodeId 节点ID
     * @param oldConfig 旧配置
     * @param newConfig 新配置
     * @param version 配置版本
     */
    void onConfigChanged(String networkId, String nodeId, WireGuardIniConfig oldConfig, WireGuardIniConfig newConfig, long version);

    /**
     * 节点下线事件
     * 
     * @param networkId 网络ID
     * @param nodeId 节点ID
     */
    void onNodeOffline(String networkId, String nodeId);

    /**
     * 网络删除事件
     * 
     * @param networkId 网络ID
     */
    void onNetworkDeleted(String networkId);
}