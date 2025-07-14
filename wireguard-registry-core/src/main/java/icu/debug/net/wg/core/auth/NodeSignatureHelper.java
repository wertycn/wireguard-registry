package icu.debug.net.wg.core.auth;

import lombok.extern.slf4j.Slf4j;
import org.whispersystems.curve25519.Curve25519;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * 节点签名助手类
 * 用于客户端生成签名
 */
@Slf4j
public class NodeSignatureHelper {

    private final Curve25519 curve25519;

    public NodeSignatureHelper() {
        this.curve25519 = Curve25519.getInstance(Curve25519.BEST);
    }

    /**
     * 使用私钥对消息进行签名
     */
    public String signMessage(String privateKey, String message) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKey);
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            
            byte[] signature = curve25519.calculateSignature(privateKeyBytes, messageBytes);
            return Base64.getEncoder().encodeToString(signature);
            
        } catch (Exception e) {
            log.error("Failed to sign message", e);
            throw new RuntimeException("Message signing failed", e);
        }
    }

    /**
     * 使用私钥对数据加时间戳进行签名
     */
    public String signWithTimestamp(String privateKey, String data, long timestamp) {
        String message = data + ":" + timestamp;
        return signMessage(privateKey, message);
    }

    /**
     * 使用私钥对数据加当前时间戳进行签名
     */
    public SignatureResult signWithCurrentTimestamp(String privateKey, String data) {
        long timestamp = Instant.now().getEpochSecond();
        String signature = signWithTimestamp(privateKey, data, timestamp);
        return new SignatureResult(signature, timestamp);
    }

    /**
     * 生成随机数
     */
    public String generateNonce() {
        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 签名结果
     */
    public static class SignatureResult {
        private final String signature;
        private final long timestamp;

        public SignatureResult(String signature, long timestamp) {
            this.signature = signature;
            this.timestamp = timestamp;
        }

        public String getSignature() {
            return signature;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}