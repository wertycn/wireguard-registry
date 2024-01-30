package icu.debug.net.wg.core.model.network;

import org.apache.commons.net.util.SubnetUtils;

/**
 * 网络地址分配器
 *
 * @author hanjinxiang@debug.icu
 * @date 2024-01-29 22:58
 */
public class NetAddressAllocator {

    private final SubnetUtils subnetUtils;

    public NetAddressAllocator(SubnetUtils subnetUtils) {
        this.subnetUtils = subnetUtils;
    }

    public NetAddressAllocator(String cidrNotation) {
        this.subnetUtils = new SubnetUtils(cidrNotation);
    }

    public NetAddressAllocator(String address, String mask) {
        this.subnetUtils = new SubnetUtils(address, mask);
    }


}
