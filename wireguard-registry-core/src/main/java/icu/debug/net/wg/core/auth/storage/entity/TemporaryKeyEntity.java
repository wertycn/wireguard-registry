package icu.debug.net.wg.core.auth.storage.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 临时密钥实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryKeyEntity {

    private String id;
    private String keyId;
    private String networkId;
    private String privateKey;
    private String publicKey;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public TemporaryKeyEntity(String keyId, String networkId, String privateKey, String publicKey, LocalDateTime expiresAt) {
        this.keyId = keyId;
        this.networkId = networkId;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }
}