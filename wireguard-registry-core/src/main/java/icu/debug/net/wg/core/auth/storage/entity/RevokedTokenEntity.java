package icu.debug.net.wg.core.auth.storage.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 撤销Token实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokedTokenEntity {

    private String id;
    private String tokenHash; // Token的SHA256哈希值，避免存储原始Token
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // Token过期时间

    public RevokedTokenEntity(String tokenHash, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }
}