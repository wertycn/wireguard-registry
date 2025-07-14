package icu.debug.net.wg.core.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * 管理员用户类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {
    
    private String username;
    private String passwordHash;
    private String email;
    private Set<AdminRole> roles;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    
    public AdminUser(String username, String passwordHash, String email, Set<AdminRole> roles, 
                    boolean active, Instant createdAt, Instant updatedAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.roles = roles;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 检查是否有指定角色
     */
    public boolean hasRole(AdminRole role) {
        return roles.contains(role) || roles.contains(AdminRole.SUPER_ADMIN);
    }
    
    /**
     * 添加角色
     */
    public void addRole(AdminRole role) {
        roles.add(role);
    }
    
    /**
     * 移除角色
     */
    public void removeRole(AdminRole role) {
        roles.remove(role);
    }
    
    /**
     * 检查是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return roles.contains(AdminRole.SUPER_ADMIN);
    }
}