package icu.debug.net.wg.core.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 临时密钥数据类
 * 用于节点初始注册的短期认证
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryKey {
    
    private String keyId;
    private String networkId;
    private String privateKey;
    private String publicKey;
    private Instant expiresAt;
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    
    public long getRemainingSeconds() {
        if (isExpired()) {
            return 0;
        }
        return expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
    }
}