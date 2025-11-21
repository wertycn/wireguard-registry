package icu.debug.net.wg.core.storage.model;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生成的配置信息 - 纯 POJO，无任何 ORM 注解
 *
 * @author WireGuard Registry Team
 * @since 1.0.0
 */
@Data
public class GeneratedConfig {

    /**
     * 所属网络ID
     */
    private String networkId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 配置对象
     */
    private WireGuardIniConfig config;

    /**
     * INI 格式配置文本
     */
    private String configText;

    /**
     * 配置版本
     */
    private Long version;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;
}
