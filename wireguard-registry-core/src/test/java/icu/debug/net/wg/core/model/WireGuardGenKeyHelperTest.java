package icu.debug.net.wg.core.model;

import icu.debug.net.wg.core.helper.WireGuardGenKeyHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WireGuardGenKeyHelperTest {

    @Test
    @DisplayName("测试私钥生成")
    void testGenPrivateKey() {
        String key = WireGuardGenKeyHelper.genPrivateKey();
        Assertions.assertNotNull(key);
    }

    @Test
    @DisplayName("测试基于私钥生成公钥")
    void testGenPubKeyByPrivateKey() {
        String privateKey = "oKcbRtbaw+wooOQ6dxe5/5yvfjht9yc13YA/SJEXfmQ=";
        String pubKey = WireGuardGenKeyHelper.genPubKeyByPrivateKey(privateKey);
        assertEquals("bVlrpsmGYeVheI5g9RPcMIjhcVkW92rKi0R2mO6X7TY=", pubKey);
    }


    @Test
    @DisplayName("测试密钥是否匹配")
    void testVerify() {
        String privateKey = "oKcbRtbaw+wooOQ6dxe5/5yvfjht9yc13YA/SJEXfmQ=";
        String publicKeyExample = "bVlrpsmGYeVheI5g9RPcMIjhcVkW92rKi0R2mO6X7TY=";
        assertTrue(WireGuardGenKeyHelper.verify(privateKey, publicKeyExample));

        String pk = WireGuardGenKeyHelper.genPrivateKey();
        String pubKey = WireGuardGenKeyHelper.genPubKeyByPrivateKey(pk);
        assertTrue(WireGuardGenKeyHelper.verify(pk, pubKey));

        assertFalse(WireGuardGenKeyHelper.verify(pk, publicKeyExample));
        assertFalse(WireGuardGenKeyHelper.verify(pk, "xxxx"));
    }

    @Test
    @DisplayName("验证密钥格式是否正确")
    void testFormatValid() {
        assertTrue(WireGuardGenKeyHelper.formatValid("oKcbRtbaw+wooOQ6dxe5/5yvfjht9yc13YA/SJEXfmQ="));
        assertFalse(WireGuardGenKeyHelper.formatValid("osKcbRtbaw+wooOQ6dxe5/5yvfjht9yc13YA/SJEXfmQ="));
    }


}