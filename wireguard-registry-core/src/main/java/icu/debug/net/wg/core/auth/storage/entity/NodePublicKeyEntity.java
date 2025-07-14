package icu.debug.net.wg.core.auth.storage.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 节点公钥实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodePublicKeyEntity {

    private String id;
    private String nodeId;
    private String publicKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NodePublicKeyEntity(String nodeId, String publicKey) {
        this.nodeId = nodeId;
        this.publicKey = publicKey;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}