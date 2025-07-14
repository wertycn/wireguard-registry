package icu.debug.net.wg.core.auth.storage;

import icu.debug.net.wg.core.auth.AdminUser;
import icu.debug.net.wg.core.auth.TemporaryKey;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 认证数据存储接口
 * 支持单机模式和集群模式
 */
public interface AuthStorage {

    // ==================== 节点认证相关 ====================
    
    /**
     * 保存节点公钥
     */
    void saveNodePublicKey(String nodeId, String publicKey);
    
    /**
     * 获取节点公钥
     */
    Optional<String> getNodePublicKey(String nodeId);
    
    /**
     * 删除节点公钥
     */
    void deleteNodePublicKey(String nodeId);
    
    /**
     * 检查节点是否已注册
     */
    boolean isNodeRegistered(String nodeId);
    
    // ==================== 临时密钥相关 ====================
    
    /**
     * 保存临时密钥
     */
    void saveTemporaryKey(TemporaryKey tempKey);
    
    /**
     * 获取临时密钥
     */
    Optional<TemporaryKey> getTemporaryKey(String keyId);
    
    /**
     * 删除临时密钥
     */
    void deleteTemporaryKey(String keyId);
    
    /**
     * 清理过期的临时密钥
     */
    void cleanupExpiredTemporaryKeys();
    
    // ==================== 管理员用户相关 ====================
    
    /**
     * 保存管理员用户
     */
    void saveAdminUser(AdminUser user);
    
    /**
     * 获取管理员用户
     */
    Optional<AdminUser> getAdminUser(String username);
    
    /**
     * 获取所有管理员用户
     */
    List<AdminUser> getAllAdminUsers();
    
    /**
     * 删除管理员用户
     */
    void deleteAdminUser(String username);
    
    /**
     * 更新管理员用户
     */
    void updateAdminUser(AdminUser user);
    
    // ==================== JWT Token相关 ====================
    
    /**
     * 撤销Token
     */
    void revokeToken(String token);
    
    /**
     * 检查Token是否被撤销
     */
    boolean isTokenRevoked(String token);
    
    /**
     * 清理过期的撤销Token
     */
    void cleanupExpiredRevokedTokens();
    
    // ==================== 集群同步相关 ====================
    
    /**
     * 获取数据版本号（用于集群同步）
     */
    long getDataVersion();
    
    /**
     * 更新数据版本号
     */
    void updateDataVersion();
    
    /**
     * 健康检查
     */
    boolean isHealthy();
}