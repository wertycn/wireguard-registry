package icu.debug.net.wg.core.model;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.config.WireGuardInterface;
import icu.debug.net.wg.core.model.config.WireGuardPeer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class WireGuardIniConfigTest {
    private static String TEST_INI_CONTENT = """
            [Interface]
            # Name = unit.test
            Address = 192.0.2.3/32
            ListenPort = 51820
            PrivateKey = localPrivateKeyAbcAbcAbc=
            
            
            [Peer]
            # Name = unit.test
            PublicKey = TestXasdfqwerqrwerq=
            AllowedIPs = 10.201.1.1/32
            PersistentKeepalive = 25 
                         
            [Peer]
            # Name = unit.test
            PublicKey = TestXasdfqwerqrwerq=
            AllowedIPs = 10.201.1.1/32
            PersistentKeepalive = 25""";

    @Test
    @DisplayName("WireGuard配置文本生成测试")
    void toIniString() {

        WireGuardInterface wgInterface = new WireGuardInterface();
        wgInterface.setName("unit.test");
        wgInterface.setAddress("192.0.2.3/32");
        wgInterface.setListenPort(51820);
        wgInterface.setPrivateKey("localPrivateKeyAbcAbcAbc=");
        WireGuardPeer peer = new WireGuardPeer();
        peer.setName("unit.test");
        peer.setPublicKey("TestXasdfqwerqrwerq=");
        peer.setAllowedIPs(Arrays.asList("10.201.1.1/32"));
        peer.setPersistentKeepalive(25);
        WireGuardIniConfig config = new WireGuardIniConfig();
        config.setName("wg0");
        config.setWgInterface(wgInterface);
        config.setPeers(Arrays.asList(peer, peer));

        assertEquals(TEST_INI_CONTENT, config.toIniString());
    }
}