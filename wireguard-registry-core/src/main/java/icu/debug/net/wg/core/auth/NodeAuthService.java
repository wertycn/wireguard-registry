package icu.debug.net.wg.core.auth;

import icu.debug.net.wg.core.helper.WireGuardGenKeyHelper;
import lombok.extern.slf4j.Slf4j;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点认证服务
 * 基于 Curve25519 公钥密码学的节点认证机制
 */
@Slf4j
public class NodeAuthService {

    private final Curve25519 curve25519;
    private final ConcurrentHashMap<String, String> nodePublicKeys = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TemporaryKey> temporaryKeys = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    
    // 临时密钥有效期（秒）
    private static final long TEMP_KEY_EXPIRY = 300; // 5分钟
    
    // 请求签名有效期（秒）
    private static final long SIGNATURE_EXPIRY = 60; // 1分钟
    
    public NodeAuthService() {
        this.curve25519 = Curve25519.getInstance(Curve25519.BEST);
    }

    /**
     * 生成临时密钥用于节点初始注册
     */
    public TemporaryKey generateTemporaryKey(String networkId) {
        String tempKeyId = generateTempKeyId();
        Curve25519KeyPair keyPair = curve25519.generateKeyPair();
        
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivateKey());
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublicKey());
        
        TemporaryKey tempKey = new TemporaryKey(
            tempKeyId,
            networkId,
            privateKey,
            publicKey,
            Instant.now().plusSeconds(TEMP_KEY_EXPIRY)
        );
        
        temporaryKeys.put(tempKeyId, tempKey);
        
        log.info("Generated temporary key {} for network {}", tempKeyId, networkId);
        return tempKey;
    }

    /**
     * 验证临时密钥签名
     */
    public boolean verifyTemporaryKeySignature(String tempKeyId, String signature, String message) {
        TemporaryKey tempKey = temporaryKeys.get(tempKeyId);
        if (tempKey == null) {
            log.warn("Temporary key {} not found", tempKeyId);
            return false;
        }
        
        if (tempKey.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Temporary key {} expired", tempKeyId);
            temporaryKeys.remove(tempKeyId);
            return false;
        }
        
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(tempKey.getPublicKey());
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            
            boolean valid = curve25519.verifySignature(publicKeyBytes, messageBytes, signatureBytes);
            
            if (valid) {
                log.debug("Temporary key signature verified for {}", tempKeyId);
            } else {
                log.warn("Invalid temporary key signature for {}", tempKeyId);
            }
            
            return valid;
        } catch (Exception e) {
            log.error("Error verifying temporary key signature", e);
            return false;
        }
    }

    /**
     * 注册节点公钥
     */
    public void registerNodePublicKey(String nodeId, String publicKey) {
        // 验证公钥格式
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
            if (publicKeyBytes.length != 32) {
                throw new IllegalArgumentException("Invalid public key length");
            }
            
            nodePublicKeys.put(nodeId, publicKey);
            log.info("Registered public key for node {}", nodeId);
            
        } catch (Exception e) {
            log.error("Invalid public key format for node {}", nodeId, e);
            throw new IllegalArgumentException("Invalid public key format", e);
        }
    }

    /**
     * 验证节点签名
     */
    public boolean verifyNodeSignature(String nodeId, String signature, String message) {
        String publicKey = nodePublicKeys.get(nodeId);
        if (publicKey == null) {
            log.warn("Public key not found for node {}", nodeId);
            return false;
        }
        
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            
            boolean valid = curve25519.verifySignature(publicKeyBytes, messageBytes, signatureBytes);
            
            if (valid) {
                log.debug("Node signature verified for {}", nodeId);
            } else {
                log.warn("Invalid node signature for {}", nodeId);
            }
            
            return valid;
        } catch (Exception e) {
            log.error("Error verifying node signature for {}", nodeId, e);
            return false;
        }
    }

    /**
     * 验证带时间戳的签名（防止重放攻击）
     */
    public boolean verifySignatureWithTimestamp(String nodeId, String signature, String data, long timestamp) {
        // 检查时间戳是否在有效范围内
        long currentTime = Instant.now().getEpochSecond();
        if (Math.abs(currentTime - timestamp) > SIGNATURE_EXPIRY) {
            log.warn("Signature timestamp expired for node {}", nodeId);
            return false;
        }
        
        // 构造要验证的消息：数据 + 时间戳
        String message = data + ":" + timestamp;
        return verifyNodeSignature(nodeId, signature, message);
    }

    /**
     * 生成签名消息
     */
    public String generateSignatureMessage(String data, long timestamp) {
        return data + ":" + timestamp;
    }

    /**
     * 获取当前时间戳
     */
    public long getCurrentTimestamp() {
        return Instant.now().getEpochSecond();
    }

    /**
     * 检查节点是否已注册
     */
    public boolean isNodeRegistered(String nodeId) {
        return nodePublicKeys.containsKey(nodeId);
    }

    /**
     * 获取节点公钥
     */
    public String getNodePublicKey(String nodeId) {
        return nodePublicKeys.get(nodeId);
    }

    /**
     * 移除节点公钥
     */
    public void removeNodePublicKey(String nodeId) {
        nodePublicKeys.remove(nodeId);
        log.info("Removed public key for node {}", nodeId);
    }

    /**
     * 清理过期的临时密钥
     */
    public void cleanupExpiredTemporaryKeys() {
        Instant now = Instant.now();
        temporaryKeys.entrySet().removeIf(entry -> {
            if (entry.getValue().getExpiresAt().isBefore(now)) {
                log.debug("Cleaned up expired temporary key {}", entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * 生成临时密钥ID
     */
    private String generateTempKeyId() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 生成请求随机数（用于防止重放攻击）
     */
    public String generateNonce() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 计算数据的SHA-256哈希
     */
    public String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error calculating hash", e);
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}