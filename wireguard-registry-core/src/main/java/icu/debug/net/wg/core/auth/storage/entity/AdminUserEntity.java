package icu.debug.net.wg.core.auth.storage.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserEntity {

    private String id;
    private String username;
    private String passwordHash;
    private String email;
    private String rolesJson; // JSON格式的角色列表
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    public AdminUserEntity(String username, String passwordHash, String email, String rolesJson, boolean active) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.rolesJson = rolesJson;
        this.active = active;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}