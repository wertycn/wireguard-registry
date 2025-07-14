package icu.debug.net.wg.core.storage.dao;

import icu.debug.net.wg.core.storage.entity.GeneratedConfigEntity;

import java.util.List;
import java.util.Optional;

/**
 * 生成配置数据访问接口
 */
public interface GeneratedConfigDao {

    /**
     * 保存生成配置
     */
    void save(GeneratedConfigEntity entity);

    /**
     * 根据网络ID和节点ID查找配置
     */
    Optional<GeneratedConfigEntity> findByNetworkIdAndNodeId(String networkId, String nodeId);

    /**
     * 根据网络ID查找所有配置
     */
    List<GeneratedConfigEntity> findByNetworkId(String networkId);

    /**
     * 删除生成配置
     */
    void deleteByNetworkIdAndNodeId(String networkId, String nodeId);

    /**
     * 删除网络下的所有配置
     */
    void deleteByNetworkId(String networkId);
}