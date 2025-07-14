package icu.debug.net.wg.core.storage.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 网络版本实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkVersionEntity {

    private String id;
    private String networkId;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NetworkVersionEntity(String networkId) {
        this.networkId = networkId;
        this.version = 1L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}