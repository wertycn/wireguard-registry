package icu.debug.net.wg.core.auth.storage.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统元数据实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetadataEntity {

    private String id;
    private String metaKey;
    private String metaValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SystemMetadataEntity(String metaKey, String metaValue) {
        this.metaKey = metaKey;
        this.metaValue = metaValue;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}