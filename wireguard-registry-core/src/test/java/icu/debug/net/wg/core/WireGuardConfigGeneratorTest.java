package icu.debug.net.wg.core;

import icu.debug.net.wg.core.helper.FileHelper;
import icu.debug.net.wg.core.model.network.LocalAreaNetwork;
import icu.debug.net.wg.core.model.network.NetworkType;
import icu.debug.net.wg.core.model.network.WireGuardNetwork;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("静态配置生成测试")
class WireGuardConfigGeneratorTest {

    @Test
    @DisplayName("测试静态配置解析")
    void testNetworkConfigParse() throws IOException {
        String result = FileHelper.readResource("wireguard-network-example.json");
        assertDoesNotThrow(() -> WireGuardNetwork.ofJson(result), "JSON 序列化解析出现异常");

        // WireGuardNetwork 解析校验
        WireGuardNetwork network = WireGuardNetwork.ofJson(result);
        assertEquals("WireGuard虚拟测试网络", network.getName());
        assertEquals(4, network.getLocalAreaNetworks().size());

        // LocalAreaNetwork 解析校验
        LocalAreaNetwork lan = network.getLocalAreaNetworks().get(0);
        assertEquals(NetworkType.OPEN_NAT, lan.getNetworkType());
        assertEquals("local", lan.getName());
        assertEquals(2, lan.getNetworkNodes().size());


        // ServerNode 解析校验
        WireGuardNetworkNode node = lan.getNetworkNodes().get(0);
        assertEquals("local-1", node.getServerNode().getHostname());
        assertEquals("181.1.1.1", node.getServerNode().getPublicAddress());
        assertEquals("10.0.4.1", node.getServerNode().getPrivateAddress());

        ;
    }

}