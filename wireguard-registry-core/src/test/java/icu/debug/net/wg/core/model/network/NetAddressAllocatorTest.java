package icu.debug.net.wg.core.model.network;

import org.apache.commons.net.util.SubnetUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-02 0:59
 */
@DisplayName("子网分配器测试")
class NetAddressAllocatorTest {
    @Test
    @DisplayName("测试实例化")
    void testNewInstance() {
        NetAddressAllocator netAddressAllocator = assertDoesNotThrow(() -> new NetAddressAllocator("10.2.1.1", "255.255.0.0"));
        NetAddressAllocator netAddressAllocator1 = assertDoesNotThrow(() -> new NetAddressAllocator("10.2.1.1/16"));
        NetAddressAllocator netAddressAllocator2 = assertDoesNotThrow(() -> new NetAddressAllocator(new SubnetUtils("10.2.1.1/16")));
        Optional<String> actual = netAddressAllocator.allocateIP();
        assertEquals(netAddressAllocator2.allocateIP(), actual);
        assertEquals(netAddressAllocator1.allocateIP(), actual);
    }

    @Test
    @DisplayName("子网判断")
    void testNetworkVerify() {
        NetAddressAllocator netAddressAllocator = assertDoesNotThrow(() -> new NetAddressAllocator("10.2.1.1", "255.255.0.0"));

        assertTrue(netAddressAllocator.isInSubnet("10.2.1.1"));
        assertFalse(netAddressAllocator.isInSubnet("10.3.1.1"));

    }

    @Test
    @DisplayName("子网分配")
    void testAllocator() {
        NetAddressAllocator netAddressAllocator = assertDoesNotThrow(() -> new NetAddressAllocator("10.2.1.1", "255.255.0.0"));

        assertFalse(netAddressAllocator.isAllocated("10.3.1.1"));
        assertFalse(netAddressAllocator.isAllocated("10.2.333.1"));
        assertFalse(netAddressAllocator.isAllocated("10.2.1.1"));
        String ip = netAddressAllocator.allocateIP().get();
        assertEquals("10.2.0.1", ip);
        assertTrue(netAddressAllocator.isAllocated(ip));

    }

    @Test
    @DisplayName("已分配子网注册")
    void testAlreadyAllocateReg() {
        NetAddressAllocator netAddressAllocator = assertDoesNotThrow(() -> new NetAddressAllocator("10.2.1.1", "255.255.0.0"));
        assertFalse(netAddressAllocator.isAllocated("10.2.0.1"));
        assertDoesNotThrow(() -> netAddressAllocator.registerAllocatedIP("10.2.0.1"));
        assertTrue(netAddressAllocator.isAllocated("10.2.0.1"));
        assertDoesNotThrow(() -> netAddressAllocator.registerAllocatedIP("10.3.1.1"));

    }

    @Test
    @DisplayName("ip分配为空 ")
    void testAllocateEmpty() {
        NetAddressAllocator netAddressAllocator = assertDoesNotThrow(() -> new NetAddressAllocator("10.2.1.1", "255.255.255.252"));
        netAddressAllocator.allocateIP();
        netAddressAllocator.allocateIP();
        netAddressAllocator.allocateIP();
        Optional<String> result = netAddressAllocator.allocateIP();
        assertTrue(result.isEmpty());

    }

    @Test
    @DisplayName("测试IP释放")
    void testIpRelease() {
        NetAddressAllocator netAddressAllocator = assertDoesNotThrow(() -> new NetAddressAllocator("10.2.1.1", "255.255.255.252"));
        String address = netAddressAllocator.allocateIP().get();
        assertTrue(netAddressAllocator.isAllocated(address));
        netAddressAllocator.releaseIP(address);
        assertFalse(netAddressAllocator.isAllocated(address));


    }
}