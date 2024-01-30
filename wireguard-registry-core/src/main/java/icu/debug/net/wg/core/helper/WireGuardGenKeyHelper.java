package icu.debug.net.wg.core.helper;

import lombok.experimental.UtilityClass;

import java.util.Base64;

/**
 * 包装Curve25519密钥生成工具，支持基于私钥生成公钥
 */
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
}
