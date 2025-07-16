package icu.debug.net.wg.service.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import icu.debug.net.wg.core.auth.AdminRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.HashSet;
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
    
    @JsonDeserialize(using = AdminRoleSetDeserializer.class)
    private Set<AdminRole> roles;
    
    /**
     * 自定义角色集合反序列化器
     * 支持从字符串、字符串数组、或对象数组反序列化
     */
    public static class AdminRoleSetDeserializer extends JsonDeserializer<Set<AdminRole>> {
        
        @Override
        public Set<AdminRole> deserialize(JsonParser p, DeserializationContext ctxt) 
                throws IOException, JsonProcessingException {
            
            JsonNode node = p.getCodec().readTree(p);
            Set<AdminRole> roles = new HashSet<>();
            
            if (node.isArray()) {
                // 处理数组格式：["SUPER_ADMIN", "NETWORK_ADMIN"] 或 [{"value": "SUPER_ADMIN"}, ...]
                for (JsonNode roleNode : node) {
                    if (roleNode.isTextual()) {
                        // 字符串数组格式
                        roles.add(AdminRole.valueOf(roleNode.asText()));
                    } else if (roleNode.isObject() && roleNode.has("value")) {
                        // 对象数组格式
                        roles.add(AdminRole.valueOf(roleNode.get("value").asText()));
                    }
                }
            } else if (node.isTextual()) {
                // 处理单个字符串格式："SUPER_ADMIN"
                roles.add(AdminRole.valueOf(node.asText()));
            }
            
            return roles;
        }
    }
}