package icu.debug.net.wg.core.storage.model;

import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 存储的网络信息 - 纯 POJO，无任何 ORM 注解
 *
 * @author WireGuard Registry Team
 * @since 1.0.0
 */
@Data
public class StoredNetwork {

    /**
     * 网络唯一标识
     */
    private String id;

    /**
     * 网络名称
     */
    private String name;

    /**
     * 网络拓扑结构
     */
    private WireGuardNetworkStruct struct;

    /**
     * 配置参数
     */
    private WireGuardNetProperties properties;

    /**
     * 配置版本号
     */
    private Long version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
