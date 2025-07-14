package icu.debug.net.wg.core.auth.storage.impl;

import icu.debug.net.wg.core.auth.AdminUser;
import icu.debug.net.wg.core.auth.TemporaryKey;
import icu.debug.net.wg.core.auth.storage.AuthStorage;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存认证存储实现
 * 适用于单机模式
 */
@Slf4j
public class MemoryAuthStorage implements AuthStorage {

    // 节点公钥存储
    private final Map<String, String> nodePublicKeys = new ConcurrentHashMap<>();
    
    // 临时密钥存储
    private final Map<String, TemporaryKey> temporaryKeys = new ConcurrentHashMap<>();
    
    // 管理员用户存储
    private final Map<String, AdminUser> adminUsers = new ConcurrentHashMap<>();
    
    // 撤销的Token存储
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    
    // 数据版本号
    private final AtomicLong dataVersion = new AtomicLong(1);

    @Override
    public void saveNodePublicKey(String nodeId, String publicKey) {
        nodePublicKeys.put(nodeId, publicKey);
        updateDataVersion();
        log.debug("Saved public key for node: {}", nodeId);
    }

    @Override
    public Optional<String> getNodePublicKey(String nodeId) {
        return Optional.ofNullable(nodePublicKeys.get(nodeId));
    }

    @Override
    public void deleteNodePublicKey(String nodeId) {
        nodePublicKeys.remove(nodeId);
        updateDataVersion();
        log.debug("Deleted public key for node: {}", nodeId);
    }

    @Override
    public boolean isNodeRegistered(String nodeId) {
        return nodePublicKeys.containsKey(nodeId);
    }

    @Override
    public void saveTemporaryKey(TemporaryKey tempKey) {
        temporaryKeys.put(tempKey.getKeyId(), tempKey);
        updateDataVersion();
        log.debug("Saved temporary key: {}", tempKey.getKeyId());
    }

    @Override
    public Optional<TemporaryKey> getTemporaryKey(String keyId) {
        TemporaryKey tempKey = temporaryKeys.get(keyId);
        if (tempKey != null && tempKey.isExpired()) {
            temporaryKeys.remove(keyId);
            log.debug("Removed expired temporary key: {}", keyId);
            return Optional.empty();
        }
        return Optional.ofNullable(tempKey);
    }

    @Override
    public void deleteTemporaryKey(String keyId) {
        temporaryKeys.remove(keyId);
        updateDataVersion();
        log.debug("Deleted temporary key: {}", keyId);
    }

    @Override
    public void cleanupExpiredTemporaryKeys() {
        Instant now = Instant.now();
        int removedCount = 0;
        
        Iterator<Map.Entry<String, TemporaryKey>> iterator = temporaryKeys.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TemporaryKey> entry = iterator.next();
            if (entry.getValue().getExpiresAt().isBefore(now)) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            updateDataVersion();
            log.debug("Cleaned up {} expired temporary keys", removedCount);
        }
    }

    @Override
    public void saveAdminUser(AdminUser user) {
        adminUsers.put(user.getUsername(), user);
        updateDataVersion();
        log.debug("Saved admin user: {}", user.getUsername());
    }

    @Override
    public Optional<AdminUser> getAdminUser(String username) {
        return Optional.ofNullable(adminUsers.get(username));
    }

    @Override
    public List<AdminUser> getAllAdminUsers() {
        return new ArrayList<>(adminUsers.values());
    }

    @Override
    public void deleteAdminUser(String username) {
        adminUsers.remove(username);
        updateDataVersion();
        log.debug("Deleted admin user: {}", username);
    }

    @Override
    public void updateAdminUser(AdminUser user) {
        if (adminUsers.containsKey(user.getUsername())) {
            adminUsers.put(user.getUsername(), user);
            updateDataVersion();
            log.debug("Updated admin user: {}", user.getUsername());
        }
    }

    @Override
    public void revokeToken(String token) {
        revokedTokens.add(token);
        updateDataVersion();
        log.debug("Revoked token");
    }

    @Override
    public boolean isTokenRevoked(String token) {
        return revokedTokens.contains(token);
    }

    @Override
    public void cleanupExpiredRevokedTokens() {
        // 内存模式下，无法判断Token是否过期，需要外部定期清理
        // 可以根据Token的创建时间来判断，但这需要解析JWT
        // 这里简单实现：如果撤销Token太多，清理一部分
        if (revokedTokens.size() > 10000) {
            int removeCount = revokedTokens.size() / 2;
            Iterator<String> iterator = revokedTokens.iterator();
            int removed = 0;
            while (iterator.hasNext() && removed < removeCount) {
                iterator.next();
                iterator.remove();
                removed++;
            }
            log.debug("Cleaned up {} revoked tokens", removed);
        }
    }

    @Override
    public long getDataVersion() {
        return dataVersion.get();
    }

    @Override
    public void updateDataVersion() {
        dataVersion.incrementAndGet();
    }

    @Override
    public boolean isHealthy() {
        return true; // 内存存储总是健康的
    }

    /**
     * 获取存储统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("nodePublicKeys", nodePublicKeys.size());
        stats.put("temporaryKeys", temporaryKeys.size());
        stats.put("adminUsers", adminUsers.size());
        stats.put("revokedTokens", revokedTokens.size());
        stats.put("dataVersion", dataVersion.get());
        return stats;
    }

    /**
     * 清空所有数据（仅用于测试）
     */
    public void clear() {
        nodePublicKeys.clear();
        temporaryKeys.clear();
        adminUsers.clear();
        revokedTokens.clear();
        dataVersion.set(1);
        log.warn("Cleared all auth storage data");
    }
}