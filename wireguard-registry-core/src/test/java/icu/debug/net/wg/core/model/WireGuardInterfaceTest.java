package icu.debug.net.wg.core.model;

import icu.debug.net.wg.core.model.config.WireGuardInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class WireGuardInterfaceTest {

    @Test
    @DisplayName("测试 WireGuard Interface 配置对象转ini配置格式")
    void toIniString() {
        WireGuardInterface wgInterface = new WireGuardInterface();
        String testIni = """
                [Interface]
                # Name = unit.test
                Address = 192.0.2.3/32
                ListenPort = 51820
                PrivateKey = localPrivateKeyAbcAbcAbc=
                DNS = 1.1.1.1,8.8.8.8
                Table = 12345
                MTU = 1500
                PreUp = /bin/example arg1 arg2 %i
                PostUp = /bin/example arg1 arg2 %i
                PreDown = /bin/example arg1 arg2 %i
                PostDown = /bin/example arg1 arg2 %i""";
        wgInterface.setName("unit.test");
        wgInterface.setAddress("192.0.2.3/32");
        wgInterface.setListenPort(51820);
        wgInterface.setPrivateKey("localPrivateKeyAbcAbcAbc=");
        wgInterface.setDns(Arrays.asList("1.1.1.1","8.8.8.8"));
        wgInterface.setMtu(1500);
        wgInterface.setTable("12345");
        wgInterface.setPreUp(Arrays.asList("/bin/example arg1 arg2 %i"));
        wgInterface.setPostUp(Arrays.asList("/bin/example arg1 arg2 %i"));
        wgInterface.setPreDown(Arrays.asList("/bin/example arg1 arg2 %i"));
        wgInterface.setPostDown(Arrays.asList("/bin/example arg1 arg2 %i"));
        String result = wgInterface.toIniString();
        assertEquals(testIni,result);
    }

    @Test
    @DisplayName("部分字段为空场景测试")
    void toIniStringWithFieldEmpty() {
        WireGuardInterface wgInterface = new WireGuardInterface();
        String testIni = """
                [Interface]
                # Name = unit.test
                Address = 192.0.2.3/32
                ListenPort = 51820
                PrivateKey = localPrivateKeyAbcAbcAbc=""";
        wgInterface.setName("unit.test");
        wgInterface.setAddress("192.0.2.3/32");
        wgInterface.setListenPort(51820);
        wgInterface.setPrivateKey("localPrivateKeyAbcAbcAbc=");
        String result = wgInterface.toIniString();
        assertEquals(testIni,result);

    }
}