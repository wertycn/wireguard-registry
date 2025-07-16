package icu.debug.net.wg.core.storage.dao;

import icu.debug.net.wg.core.storage.entity.NetworkVersionEntity;

import java.util.List;
import java.util.Optional;

/**
 * 网络版本数据访问接口
 */
public interface NetworkVersionDao {

    /**
     * 保存网络版本
     */
    void save(NetworkVersionEntity entity);

    /**
     * 根据网络ID查找版本
     */
    Optional<NetworkVersionEntity> findByNetworkId(String networkId);

    /**
     * 删除网络版本
     */
    void deleteByNetworkId(String networkId);

    /**
     * 递增网络版本
     */
    void incrementVersion(String networkId);

    /**
     * 获取所有网络ID
     */
    List<String> findAllNetworkIds();
}