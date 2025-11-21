package icu.debug.net.wg.storage.sqlite.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wg_configs", indexes = {
    @Index(name = "idx_network_node", columnList = "networkId,nodeId", unique = true)
})
public class ConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String networkId;

    @Column(nullable = false)
    private String nodeId;

    private String nodeName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String configJson;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String configText;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
