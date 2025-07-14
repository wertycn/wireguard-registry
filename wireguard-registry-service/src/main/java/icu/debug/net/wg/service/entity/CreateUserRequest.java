package icu.debug.net.wg.service.entity;

import icu.debug.net.wg.core.auth.AdminRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * 创建用户请求实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    private String username;
    private String password;
    private String email;
    private Set<AdminRole> roles;
}