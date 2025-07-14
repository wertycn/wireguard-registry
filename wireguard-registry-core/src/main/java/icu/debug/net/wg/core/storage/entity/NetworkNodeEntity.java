package icu.debug.net.wg.core.storage.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 网络节点实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkNodeEntity {

    private String id;
    private String networkId;
    private String nodeId;
    private String nodeData; // JSON格式的节点数据
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public NetworkNodeEntity(String networkId, String nodeId, String nodeData) {
        this.networkId = networkId;
        this.nodeId = nodeId;
        this.nodeData = nodeData;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 1L;
    }
}