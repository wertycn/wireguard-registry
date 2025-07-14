package icu.debug.net.wg.core.auth;

/**
 * 管理员角色枚举
 */
public enum AdminRole {
    
    /**
     * 超级管理员 - 拥有所有权限
     */
    SUPER_ADMIN("超级管理员", "拥有所有权限，可以管理用户和系统设置"),
    
    /**
     * 网络管理员 - 可以管理网络和节点
     */
    NETWORK_ADMIN("网络管理员", "可以创建、删除、修改网络和节点"),
    
    /**
     * 节点管理员 - 可以管理节点
     */
    NODE_ADMIN("节点管理员", "可以管理节点，但不能创建或删除网络"),
    
    /**
     * 监控员 - 只能查看监控数据
     */
    MONITOR("监控员", "只能查看系统监控数据和日志"),
    
    /**
     * 只读用户 - 只能查看配置信息
     */
    READ_ONLY("只读用户", "只能查看配置信息，不能进行任何修改操作");
    
    private final String displayName;
    private final String description;
    
    AdminRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 检查角色是否有指定权限
     */
    public boolean hasPermission(AdminPermission permission) {
        if (this == SUPER_ADMIN) {
            return true; // 超级管理员拥有所有权限
        }
        
        return switch (this) {
            case NETWORK_ADMIN -> switch (permission) {
                case CREATE_NETWORK, DELETE_NETWORK, UPDATE_NETWORK,
                     CREATE_NODE, DELETE_NODE, UPDATE_NODE,
                     VIEW_NETWORK, VIEW_NODE, VIEW_CONFIG -> true;
                default -> false;
            };
            case NODE_ADMIN -> switch (permission) {
                case CREATE_NODE, DELETE_NODE, UPDATE_NODE,
                     VIEW_NETWORK, VIEW_NODE, VIEW_CONFIG -> true;
                default -> false;
            };
            case MONITOR -> switch (permission) {
                case VIEW_NETWORK, VIEW_NODE, VIEW_CONFIG, VIEW_LOGS -> true;
                default -> false;
            };
            case READ_ONLY -> switch (permission) {
                case VIEW_NETWORK, VIEW_NODE, VIEW_CONFIG -> true;
                default -> false;
            };
            default -> false;
        };
    }
    
    /**
     * 权限枚举
     */
    public enum AdminPermission {
        // 网络权限
        CREATE_NETWORK,
        DELETE_NETWORK,
        UPDATE_NETWORK,
        VIEW_NETWORK,
        
        // 节点权限
        CREATE_NODE,
        DELETE_NODE,
        UPDATE_NODE,
        VIEW_NODE,
        
        // 配置权限
        VIEW_CONFIG,
        GENERATE_CONFIG,
        
        // 用户权限
        CREATE_USER,
        DELETE_USER,
        UPDATE_USER,
        VIEW_USER,
        
        // 系统权限
        VIEW_LOGS,
        SYSTEM_CONFIG,
        
        // 临时密钥权限
        GENERATE_TEMP_KEY,
        REVOKE_TEMP_KEY
    }
}