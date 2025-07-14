package icu.debug.net.wg.core.auth.storage.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import icu.debug.net.wg.core.auth.AdminRole;
import icu.debug.net.wg.core.auth.AdminUser;
import icu.debug.net.wg.core.auth.TemporaryKey;
import icu.debug.net.wg.core.auth.storage.AuthStorage;
import icu.debug.net.wg.core.auth.storage.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库认证存储实现
 * 适用于集群模式，完全无状态
 */
@Slf4j
public class DatabaseAuthStorage implements AuthStorage {

    private final AuthStorageDao authStorageDao;
    private final ObjectMapper objectMapper;
    private static final String DATA_VERSION_KEY = "auth_data_version";

    public DatabaseAuthStorage(AuthStorageDao authStorageDao, ObjectMapper objectMapper) {
        this.authStorageDao = authStorageDao;
        this.objectMapper = objectMapper;
        
        // 初始化数据版本
        initializeDataVersion();
    }

    @Override
    public void saveNodePublicKey(String nodeId, String publicKey) {
        try {
            Optional<NodePublicKeyEntity> existing = authStorageDao.findNodePublicKeyByNodeId(nodeId);
            if (existing.isPresent()) {
                NodePublicKeyEntity entity = existing.get();
                entity.setPublicKey(publicKey);
                entity.setUpdatedAt(LocalDateTime.now());
                authStorageDao.updateNodePublicKey(entity);
            } else {
                NodePublicKeyEntity entity = new NodePublicKeyEntity(nodeId, publicKey);
                authStorageDao.saveNodePublicKey(entity);
            }
            updateDataVersion();
            log.debug("Saved public key for node: {}", nodeId);
        } catch (Exception e) {
            log.error("Failed to save public key for node: {}", nodeId, e);
            throw new RuntimeException("Failed to save public key", e);
        }
    }

    @Override
    public Optional<String> getNodePublicKey(String nodeId) {
        try {
            return authStorageDao.findNodePublicKeyByNodeId(nodeId)
                    .map(NodePublicKeyEntity::getPublicKey);
        } catch (Exception e) {
            log.error("Failed to get public key for node: {}", nodeId, e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteNodePublicKey(String nodeId) {
        try {
            authStorageDao.deleteNodePublicKeyByNodeId(nodeId);
            updateDataVersion();
            log.debug("Deleted public key for node: {}", nodeId);
        } catch (Exception e) {
            log.error("Failed to delete public key for node: {}", nodeId, e);
            throw new RuntimeException("Failed to delete public key", e);
        }
    }

    @Override
    public boolean isNodeRegistered(String nodeId) {
        try {
            return authStorageDao.existsNodePublicKeyByNodeId(nodeId);
        } catch (Exception e) {
            log.error("Failed to check if node is registered: {}", nodeId, e);
            return false;
        }
    }

    @Override
    public void saveTemporaryKey(TemporaryKey tempKey) {
        try {
            LocalDateTime expiresAt = LocalDateTime.ofInstant(tempKey.getExpiresAt(), ZoneId.systemDefault());
            TemporaryKeyEntity entity = new TemporaryKeyEntity(
                    tempKey.getKeyId(),
                    tempKey.getNetworkId(),
                    tempKey.getPrivateKey(),
                    tempKey.getPublicKey(),
                    expiresAt
            );
            authStorageDao.saveTemporaryKey(entity);
            updateDataVersion();
            log.debug("Saved temporary key: {}", tempKey.getKeyId());
        } catch (Exception e) {
            log.error("Failed to save temporary key: {}", tempKey.getKeyId(), e);
            throw new RuntimeException("Failed to save temporary key", e);
        }
    }

    @Override
    public Optional<TemporaryKey> getTemporaryKey(String keyId) {
        try {
            return authStorageDao.findTemporaryKeyByKeyId(keyId)
                    .map(this::convertToTemporaryKey)
                    .filter(tempKey -> !tempKey.isExpired());
        } catch (Exception e) {
            log.error("Failed to get temporary key: {}", keyId, e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteTemporaryKey(String keyId) {
        try {
            authStorageDao.deleteTemporaryKeyByKeyId(keyId);
            updateDataVersion();
            log.debug("Deleted temporary key: {}", keyId);
        } catch (Exception e) {
            log.error("Failed to delete temporary key: {}", keyId, e);
            throw new RuntimeException("Failed to delete temporary key", e);
        }
    }

    @Override
    public void cleanupExpiredTemporaryKeys() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int removedCount = authStorageDao.deleteExpiredTemporaryKeys(now);
            if (removedCount > 0) {
                updateDataVersion();
                log.debug("Cleaned up {} expired temporary keys", removedCount);
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired temporary keys", e);
        }
    }

    @Override
    public void saveAdminUser(AdminUser user) {
        try {
            String rolesJson = objectMapper.writeValueAsString(user.getRoles());
            
            Optional<AdminUserEntity> existing = authStorageDao.findAdminUserByUsername(user.getUsername());
            if (existing.isPresent()) {
                AdminUserEntity entity = existing.get();
                entity.setPasswordHash(user.getPasswordHash());
                entity.setEmail(user.getEmail());
                entity.setRolesJson(rolesJson);
                entity.setActive(user.isActive());
                entity.setUpdatedAt(LocalDateTime.now());
                if (user.getLastLoginAt() != null) {
                    entity.setLastLoginAt(LocalDateTime.ofInstant(user.getLastLoginAt(), ZoneId.systemDefault()));
                }
                authStorageDao.updateAdminUser(entity);
            } else {
                AdminUserEntity entity = new AdminUserEntity(
                        user.getUsername(),
                        user.getPasswordHash(),
                        user.getEmail(),
                        rolesJson,
                        user.isActive()
                );
                authStorageDao.saveAdminUser(entity);
            }
            updateDataVersion();
            log.debug("Saved admin user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to save admin user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to save admin user", e);
        }
    }

    @Override
    public Optional<AdminUser> getAdminUser(String username) {
        try {
            return authStorageDao.findAdminUserByUsername(username)
                    .map(this::convertToAdminUser);
        } catch (Exception e) {
            log.error("Failed to get admin user: {}", username, e);
            return Optional.empty();
        }
    }

    @Override
    public List<AdminUser> getAllAdminUsers() {
        try {
            return authStorageDao.findAllAdminUsers()
                    .stream()
                    .map(this::convertToAdminUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all admin users", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteAdminUser(String username) {
        try {
            authStorageDao.deleteAdminUserByUsername(username);
            updateDataVersion();
            log.debug("Deleted admin user: {}", username);
        } catch (Exception e) {
            log.error("Failed to delete admin user: {}", username, e);
            throw new RuntimeException("Failed to delete admin user", e);
        }
    }

    @Override
    public void updateAdminUser(AdminUser user) {
        saveAdminUser(user); // 在数据库实现中，save和update是一样的
    }

    @Override
    public void revokeToken(String token) {
        try {
            String tokenHash = calculateTokenHash(token);
            // 计算Token过期时间（从JWT中解析或使用默认值）
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // 默认24小时后过期
            
            RevokedTokenEntity entity = new RevokedTokenEntity(tokenHash, expiresAt);
            authStorageDao.saveRevokedToken(entity);
            updateDataVersion();
            log.debug("Revoked token");
        } catch (Exception e) {
            log.error("Failed to revoke token", e);
            throw new RuntimeException("Failed to revoke token", e);
        }
    }

    @Override
    public boolean isTokenRevoked(String token) {
        try {
            String tokenHash = calculateTokenHash(token);
            return authStorageDao.existsRevokedTokenByTokenHash(tokenHash);
        } catch (Exception e) {
            log.error("Failed to check if token is revoked", e);
            return false;
        }
    }

    @Override
    public void cleanupExpiredRevokedTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int removedCount = authStorageDao.deleteExpiredRevokedTokens(now);
            if (removedCount > 0) {
                log.debug("Cleaned up {} expired revoked tokens", removedCount);
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired revoked tokens", e);
        }
    }

    @Override
    public long getDataVersion() {
        try {
            return authStorageDao.findSystemMetadataByKey(DATA_VERSION_KEY)
                    .map(entity -> Long.parseLong(entity.getMetaValue()))
                    .orElse(1L);
        } catch (Exception e) {
            log.error("Failed to get data version", e);
            return 1L;
        }
    }

    @Override
    public void updateDataVersion() {
        try {
            long newVersion = getDataVersion() + 1;
            
            Optional<SystemMetadataEntity> existing = authStorageDao.findSystemMetadataByKey(DATA_VERSION_KEY);
            if (existing.isPresent()) {
                SystemMetadataEntity entity = existing.get();
                entity.setMetaValue(String.valueOf(newVersion));
                entity.setUpdatedAt(LocalDateTime.now());
                authStorageDao.updateSystemMetadata(entity);
            } else {
                SystemMetadataEntity entity = new SystemMetadataEntity(DATA_VERSION_KEY, String.valueOf(newVersion));
                authStorageDao.saveSystemMetadata(entity);
            }
        } catch (Exception e) {
            log.error("Failed to update data version", e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // 简单的健康检查：尝试获取数据版本
            getDataVersion();
            return true;
        } catch (Exception e) {
            log.error("Database auth storage health check failed", e);
            return false;
        }
    }

    /**
     * 初始化数据版本
     */
    private void initializeDataVersion() {
        try {
            if (!authStorageDao.findSystemMetadataByKey(DATA_VERSION_KEY).isPresent()) {
                SystemMetadataEntity entity = new SystemMetadataEntity(DATA_VERSION_KEY, "1");
                authStorageDao.saveSystemMetadata(entity);
                log.info("Initialized auth data version");
            }
        } catch (Exception e) {
            log.error("Failed to initialize data version", e);
        }
    }

    /**
     * 转换实体类到业务对象
     */
    private TemporaryKey convertToTemporaryKey(TemporaryKeyEntity entity) {
        Instant expiresAt = entity.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant();
        return new TemporaryKey(
                entity.getKeyId(),
                entity.getNetworkId(),
                entity.getPrivateKey(),
                entity.getPublicKey(),
                expiresAt
        );
    }

    /**
     * 转换实体类到业务对象
     */
    private AdminUser convertToAdminUser(AdminUserEntity entity) {
        try {
            Set<AdminRole> roles = objectMapper.readValue(entity.getRolesJson(), new TypeReference<Set<AdminRole>>() {});
            
            Instant createdAt = entity.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant();
            Instant updatedAt = entity.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant();
            Instant lastLoginAt = entity.getLastLoginAt() != null ? 
                    entity.getLastLoginAt().atZone(ZoneId.systemDefault()).toInstant() : null;
            
            return new AdminUser(
                    entity.getUsername(),
                    entity.getPasswordHash(),
                    entity.getEmail(),
                    roles,
                    entity.isActive(),
                    createdAt,
                    updatedAt,
                    lastLoginAt
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse roles JSON for user: {}", entity.getUsername(), e);
            throw new RuntimeException("Failed to parse user roles", e);
        }
    }

    /**
     * 计算Token哈希值
     */
    private String calculateTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to calculate token hash", e);
            throw new RuntimeException("Failed to calculate token hash", e);
        }
    }

    /**
     * 数据库访问接口
     */
    public interface AuthStorageDao {
        // 节点公钥相关
        void saveNodePublicKey(NodePublicKeyEntity entity);
        void updateNodePublicKey(NodePublicKeyEntity entity);
        Optional<NodePublicKeyEntity> findNodePublicKeyByNodeId(String nodeId);
        void deleteNodePublicKeyByNodeId(String nodeId);
        boolean existsNodePublicKeyByNodeId(String nodeId);

        // 临时密钥相关
        void saveTemporaryKey(TemporaryKeyEntity entity);
        Optional<TemporaryKeyEntity> findTemporaryKeyByKeyId(String keyId);
        void deleteTemporaryKeyByKeyId(String keyId);
        int deleteExpiredTemporaryKeys(LocalDateTime expiredBefore);

        // 管理员用户相关
        void saveAdminUser(AdminUserEntity entity);
        void updateAdminUser(AdminUserEntity entity);
        Optional<AdminUserEntity> findAdminUserByUsername(String username);
        List<AdminUserEntity> findAllAdminUsers();
        void deleteAdminUserByUsername(String username);

        // 撤销Token相关
        void saveRevokedToken(RevokedTokenEntity entity);
        boolean existsRevokedTokenByTokenHash(String tokenHash);
        int deleteExpiredRevokedTokens(LocalDateTime expiredBefore);

        // 系统元数据相关
        void saveSystemMetadata(SystemMetadataEntity entity);
        void updateSystemMetadata(SystemMetadataEntity entity);
        Optional<SystemMetadataEntity> findSystemMetadataByKey(String metaKey);
    }
}