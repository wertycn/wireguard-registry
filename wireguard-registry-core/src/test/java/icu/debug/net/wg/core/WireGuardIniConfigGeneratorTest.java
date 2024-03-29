package icu.debug.net.wg.core;

import icu.debug.net.wg.core.helper.FileHelper;
import icu.debug.net.wg.core.helper.WireGuardGenKeyHelper;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.config.WireGuardInterface;
import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.config.WireGuardPeer;
import icu.debug.net.wg.core.model.network.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("静态配置生成测试")
class WireGuardIniConfigGeneratorTest {

    @SneakyThrows
    private static WireGuardNetworkStruct getWireGuardNetworkStruct() {
        return getWireGuardNetworkStruct("wireguard-network-example.json");
    }

    @SneakyThrows
    private static WireGuardNetworkStruct getWireGuardNetworkStruct(String name) {
        String result = FileHelper.readResource(name);
        return WireGuardNetworkStruct.ofJson(result);
    }


    @Test
    @DisplayName("测试静态配置内容解析")
    void test() throws IOException {
        String result = FileHelper.readResource("wireguard-network-example.json");
        assertDoesNotThrow(() -> WireGuardNetworkStruct.ofJson(result), "JSON 序列化解析出现异常");
        assertThrows(IllegalArgumentException.class, () -> WireGuardNetworkStruct.ofJson("xx"), "异常JSON 序列化解析校验");
    }

    @Test
    @DisplayName("测试配置属性解析")
    void testNetworkConfigParse() {
        // WireGuardNetwork 解析校验
        WireGuardNetworkStruct network = getWireGuardNetworkStruct();
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
        assertEquals("10.201.1.1", node.getAddress());

        assertEquals("bVlrpsmGYeVheI5g9RPcMIjhcVkW92rKi0R2mO6X7TY=", node.getPublicKey());
        assertEquals("oKcbRtbaw+wooOQ6dxe5/5yvfjht9yc13YA/SJEXfmQ=", node.getPrivateKey());
        assertEquals(5219, node.getListenPort());

        assertEquals("auto", node.getTable());
        assertEquals(1420, node.getMtu());
        assertEquals(2, node.getDns().size());
        assertEquals("8.8.8.8", node.getDns().get(0));
        assertEquals("echo 'Hello! WireGuard Registry'", node.getPreUp().get(0));
        assertTrue(node.getPostUp().isEmpty());
        assertTrue(node.getPostDown().isEmpty());
        assertTrue(node.getPreDown().isEmpty());
    }


    @Test
    @DisplayName("测试获取网络结构下所有网络节点")
    void testGetNetworkNodes() {
        WireGuardNetworkStruct struct = getWireGuardNetworkStruct();
        // 获取所有网络节点
        List<WireGuardNetworkNode> networkNodes = struct.getNetworkNodes();
        assertEquals(7, networkNodes.size());

        List<ServerNode> serverNodes = struct.getServerNodes();
        assertEquals(7, serverNodes.size());
        assertEquals("group-tcloud-b-01", serverNodes.get(5).getHostname());

    }

    @Test
    @DisplayName("测试生成WireGuard Interface")
    void testToInterface() {
        WireGuardNetworkNode node = getNetworkNode();

        WireGuardInterface wgInterface = node.toInterface();
        assertEquals("local-1", wgInterface.getName());
        assertEquals("oKcbRtbaw+wooOQ6dxe5/5yvfjht9yc13YA/SJEXfmQ=", wgInterface.getPrivateKey());

        assertEquals("10.201.1.1", wgInterface.getAddress());
        assertEquals(5219, wgInterface.getListenPort());
        assertEquals("auto", wgInterface.getTable());
        assertEquals(1420, wgInterface.getMtu());
        assertEquals(2, wgInterface.getDns().size());
        assertEquals("8.8.8.8", wgInterface.getDns().get(0));
        assertEquals("echo 'Hello! WireGuard Registry'", wgInterface.getPreUp().get(0));
        assertTrue(wgInterface.getPostUp().isEmpty());
        assertTrue(wgInterface.getPostDown().isEmpty());
        assertTrue(wgInterface.getPreDown().isEmpty());
    }

    private static WireGuardNetworkNode getNetworkNode() {
        WireGuardNetworkStruct struct = getWireGuardNetworkStruct();
        return struct.getNetworkNodes().get(0);
    }

    @Test
    @DisplayName("测试生成Peer-公网端点")
    void testToPeerWithPublic() {
        WireGuardNetworkNode node = getNetworkNode();

        WireGuardPeer peer = node.toPeer(EndpointType.PUBLIC);
        assertEquals("local-1", peer.getName());
        assertEquals("10.201.1.1", peer.getAllowedIPs().get(0));
        assertEquals("181.1.1.1:5219", peer.getEndpoint());
        String peerIni = """
                [Peer]
                # Name = local-1
                Endpoint = 181.1.1.1:5219
                PublicKey = bVlrpsmGYeVheI5g9RPcMIjhcVkW92rKi0R2mO6X7TY=
                AllowedIPs = 10.201.1.1""";

        assertEquals(peerIni, peer.toIniString());

    }

    @Test
    @DisplayName("测试生成Peer-内网端点")
    void testToPeerWithPrivate() {
        WireGuardNetworkNode node = getNetworkNode();

        WireGuardPeer peer = node.toPeer(EndpointType.PRIVATE);
        assertEquals("local-1", peer.getName());
        assertEquals("10.201.1.1", peer.getAllowedIPs().get(0));
        assertEquals("10.0.4.1:5219", peer.getEndpoint());
        String peerIni = """
                [Peer]
                # Name = local-1
                Endpoint = 10.0.4.1:5219
                PublicKey = bVlrpsmGYeVheI5g9RPcMIjhcVkW92rKi0R2mO6X7TY=
                AllowedIPs = 10.201.1.1""";

        assertEquals(peerIni, peer.toIniString());
    }


    @Test
    @DisplayName("测试生成Peer-内网端点")
    void testToPeerWithUnknown() {
        WireGuardNetworkNode node = getNetworkNode();

        WireGuardPeer peer = node.toPeer(EndpointType.UNKNOWN);
        assertEquals("local-1", peer.getName());
        assertEquals("10.201.1.1", peer.getAllowedIPs().get(0));
        assertNull(peer.getEndpoint());
        String peerIni = """
                [Peer]
                # Name = local-1
                PublicKey = bVlrpsmGYeVheI5g9RPcMIjhcVkW92rKi0R2mO6X7TY=
                AllowedIPs = 10.201.1.1""";

        assertEquals(peerIni, peer.toIniString());
    }

    @Test
    @DisplayName("指定节点获取WireGuard组网配置")
    void testGetIniConfigByHostname() {
        WireGuardNetProperties properties = getWireGuardNetProperties();
        WireGuardConfigGenerator generator = new WireGuardConfigGenerator(getWireGuardNetworkStruct(), properties);
        WireGuardIniConfig wireGuardIniConfig = generator.buildWireGuardIniConfig("local-1");
        assertNotNull(wireGuardIniConfig.toIniString());
        String privateKey = wireGuardIniConfig.getWgInterface().getPrivateKey();
        List<WireGuardPeer> peers = wireGuardIniConfig.getPeers().stream().filter(peer -> peer.getName().equals("local-1")).toList();
        assertEquals(1, peers.size());
        peers.forEach(peer -> {
            WireGuardGenKeyHelper.verify(privateKey, peer.getPublicKey());
        });
        System.out.println(wireGuardIniConfig.toIniString());

    }

    @Test
    @DisplayName("测试指定子网地址节点配置生成")
    void testSetSubNetGen() {
        WireGuardNetProperties properties = getWireGuardNetProperties();
        WireGuardNetworkStruct struct = getWireGuardNetworkStruct("wireguard-network-example-v3.json");
        WireGuardConfigGenerator generator = new WireGuardConfigGenerator(struct, properties);
        assertEquals("10.201.0.3", generator.buildConfig("set-sub-net").getConfig().getWgInterface().getAddress());
        assertEquals("10.201.0.1", generator.buildConfig("sub-not-valid-net").getConfig().getWgInterface().getAddress());
        assertEquals("10.201.0.2", generator.buildConfig("not-set-sub-net").getConfig().getWgInterface().getAddress());
    }

    @Test
    @DisplayName("测试心跳时长配置")
    void testKeepAliveTime() {
        WireGuardNetProperties properties = getWireGuardNetProperties();
        WireGuardNetworkStruct struct = getWireGuardNetworkStruct("wireguard-network-example-keepalive.json");
        WireGuardConfigGenerator generator = new WireGuardConfigGenerator(struct, properties);
        generator.buildConfig("set-keepalive").getConfig().getPeers().forEach(peer -> assertEquals(55, peer.getPersistentKeepalive()));
        generator.buildConfig("not-set-keepalive").getConfig().getPeers().forEach(peer -> assertNull(peer.getPersistentKeepalive()));
    }

    private static WireGuardNetProperties getWireGuardNetProperties() {
        WireGuardNetProperties properties = new WireGuardNetProperties();
        properties.setNetmask("255.255.255.0");
        properties.setAddress("10.201.0.1");
        return properties;
    }

    @Test
    void buildWireGuardIniConfigs() {

        WireGuardNetProperties properties = getWireGuardNetProperties();
        WireGuardConfigGenerator generator = new WireGuardConfigGenerator(getWireGuardNetworkStruct(), properties);

        Map<String, WireGuardIniConfig> stringWireGuardIniConfigMap = generator.buildWireGuardIniConfigMap();
        stringWireGuardIniConfigMap.forEach((s, wireGuardIniConfig) -> {
            assertNotNull(wireGuardIniConfig.toIniString());
            String privateKey = wireGuardIniConfig.getWgInterface().getPrivateKey();
            List<WireGuardPeer> peers = wireGuardIniConfig.getPeers().stream().filter(peer -> peer.getName().equals(s)).toList();
            assertEquals(1, peers.size());
            peers.forEach(peer -> {
                WireGuardGenKeyHelper.verify(privateKey, peer.getPublicKey());
            });
            System.out.println("=========================");
            System.out.println(s);
            System.out.println(wireGuardIniConfig.toIniString());
            System.out.println("=========================");

        });
    }

    @Test
    @DisplayName("忽略默认属性测试")
    void testIgnoreDefaultMerge() throws IOException {
        WireGuardNetProperties properties = getWireGuardNetProperties();

        WireGuardConfigGenerator generator = new WireGuardConfigGenerator(getWireGuardNetworkStruct("wireguard-network-example-v2.json"), properties);
        Map<String, WireGuardIniConfig> stringWireGuardIniConfigMap = generator.buildWireGuardIniConfigMap();
        assertNull(stringWireGuardIniConfigMap.get("group-tcloud-a-01").getWgInterface().getPrivateKey());
    }

}