package icu.debug.net.wg.core.helper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * 包装Curve25519密钥生成工具，支持基于私钥生成公钥
 */
@Slf4j
@UtilityClass
public class WireGuardGenKeyHelper {

    private static final Curve25519OpenProvider PROVIDER = new Curve25519OpenProvider();

    public static String genPrivateKey() {
        byte[] bytes = PROVIDER.generatePrivateKey();
        return Base64.getEncoder().encodeToString(bytes);
    }


    public static String genPubKeyByPrivateKey(String privateKey) {
        byte[] bytes = Base64.getDecoder().decode(privateKey);
        byte[] publicKey = PROVIDER.generatePublicKey(bytes);
        return Base64.getEncoder().encodeToString(publicKey);
    }

    public static boolean verify(String privateKey, String publicKey) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKey);
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
            byte[] message = new byte[128 * 128];
            byte[] signature = PROVIDER.calculateSignature(PROVIDER.getRandom(64), privateKeyBytes, message);
            return PROVIDER.verifySignature(publicKeyBytes, message, signature);
        } catch (Exception e) {
            log.warn("sign verify error private key {} public key {} :{}", privateKey, publicKey, e.getMessage());
            return false;
        }
    }

    public static boolean formatValid(String key) {
        try {
            byte[] bytes = Base64.getDecoder().decode(key);
            return bytes.length == 32;
        } catch (Exception e) {
            log.warn("Invalid private key [{}]", key);
            return false;
        }
    }
}
