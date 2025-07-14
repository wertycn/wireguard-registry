package icu.debug.net.wg.core.storage.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 生成配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedConfigEntity {

    private String id;
    private String networkId;
    private String nodeId;
    private String configData; // JSON格式的配置数据
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public GeneratedConfigEntity(String networkId, String nodeId, String configData) {
        this.networkId = networkId;
        this.nodeId = nodeId;
        this.configData = configData;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 1L;
    }
}