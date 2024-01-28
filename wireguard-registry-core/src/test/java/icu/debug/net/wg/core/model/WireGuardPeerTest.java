package icu.debug.net.wg.core.model;

import icu.debug.net.wg.core.model.config.WireGuardPeer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class WireGuardPeerTest {

    private static String PEER_EXAMPLE = """
            [Peer]
            # Name = unit.test
            Endpoint = unit.test:51820
            PublicKey = TestXasdfqwerqrwerq=
            AllowedIPs = 10.201.1.1/32
            PersistentKeepalive = 25""";

    @Test
    @DisplayName("Peer转ini格式文本测试")
    void toIniString() {
        WireGuardPeer peer = new WireGuardPeer();
        peer.setName("unit.test");
        peer.setEndpoint("unit.test:51820");
        peer.setPublicKey("TestXasdfqwerqrwerq=");
        peer.setAllowedIPs(Arrays.asList("10.201.1.1/32"));
        peer.setPersistentKeepalive(25);
        assertEquals(PEER_EXAMPLE, peer.toIniString());
    }

    @Test
    @DisplayName("Peer转ini配置部分字段为空场景测试")
    void toIniStringWithFieldEmpty() {
        WireGuardPeer peer = new WireGuardPeer();
        peer.setName("unit.test");
        peer.setPublicKey("TestXasdfqwerqrwerq=");
        peer.setAllowedIPs(Arrays.asList("10.201.1.1/32"));
        peer.setPersistentKeepalive(25);
        assertEquals("""
                        [Peer]
                        # Name = unit.test
                        PublicKey = TestXasdfqwerqrwerq=
                        AllowedIPs = 10.201.1.1/32
                        PersistentKeepalive = 25""",
                peer.toIniString()
        );
    }
}