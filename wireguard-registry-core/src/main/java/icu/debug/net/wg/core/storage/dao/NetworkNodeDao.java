package icu.debug.net.wg.core.storage.dao;

import icu.debug.net.wg.core.storage.entity.NetworkNodeEntity;

import java.util.List;
import java.util.Optional;

/**
 * 网络节点数据访问接口
 */
public interface NetworkNodeDao {

    /**
     * 保存网络节点
     */
    void save(NetworkNodeEntity entity);

    /**
     * 根据网络ID和节点ID查找节点
     */
    Optional<NetworkNodeEntity> findByNetworkIdAndNodeId(String networkId, String nodeId);

    /**
     * 根据网络ID查找所有节点
     */
    List<NetworkNodeEntity> findByNetworkId(String networkId);

    /**
     * 删除网络节点
     */
    void deleteByNetworkIdAndNodeId(String networkId, String nodeId);

    /**
     * 删除网络下的所有节点
     */
    void deleteByNetworkId(String networkId);

    /**
     * 获取所有网络ID
     */
    List<String> findAllNetworkIds();

    /**
     * 检查网络是否存在
     */
    boolean existsByNetworkId(String networkId);
}