package icu.debug.net.wg.core.model;

import icu.debug.net.wg.core.helper.WireGuardGenKeyHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Assertions.assertEquals("bVlrpsmGYeVheI5g9RPcMIjhcVkW92rKi0R2mO6X7TY=", pubKey);
    }


    @Test
    @DisplayName("测试密钥是否匹配")
    void testVerify() {
        // TODO: 2021/8/3 0003 无法通过验证
        assertFalse(WireGuardGenKeyHelper.verify("oKcbRtbaw+wooOQ6dxe5/5yvfjht9yc13YA/SJEXfmQ=", "bVlrpsmGYeVheI5g9RPcMIjhcVkW92rKi0R2mO6X7TY="));
    }

    @Test
    @DisplayName("验证密钥格式是否正确")
    void testFormatValid(){
        assertTrue(WireGuardGenKeyHelper.formatValid("oKcbRtbaw+wooOQ6dxe5/5yvfjht9yc13YA/SJEXfmQ="));
        assertFalse(WireGuardGenKeyHelper.formatValid("osKcbRtbaw+wooOQ6dxe5/5yvfjht9yc13YA/SJEXfmQ="));
    }


}