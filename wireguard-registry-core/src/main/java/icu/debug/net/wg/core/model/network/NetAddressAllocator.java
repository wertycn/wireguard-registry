package icu.debug.net.wg.core.model.network;

import org.apache.commons.net.util.SubnetUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 网络地址分配器
 *
 * @author hanjinxiang@debug.icu
 * @date 2024-01-29 22:58
 */
public class NetAddressAllocator {

    private final SubnetUtils subnetUtils;

    private Set<String> allocatedIPs = new LinkedHashSet<>();


    public NetAddressAllocator(SubnetUtils subnetUtils) {
        this.subnetUtils = subnetUtils;
    }

    public NetAddressAllocator(String cidrNotation) {
        this.subnetUtils = new SubnetUtils(cidrNotation);
    }

    public NetAddressAllocator(String address, String mask) {
        this.subnetUtils = new SubnetUtils(address, mask);
    }

    // 判断 IP 地址是否属于当前子网
    public boolean isInSubnet(String ip) {
        return this.subnetUtils.getInfo().isInRange(ip);
    }

    // 判断 IP 是否已经分配
    public boolean isAllocated(String ip) {
        return this.allocatedIPs.contains(ip);
    }

    // 注册已经分配的 IP 地址
    public void registerAllocatedIP(String ip) {
        if (isInSubnet(ip) && !isAllocated(ip)) {
            this.allocatedIPs.add(ip);
        }
    }

    // 注册已经分配的 IP 地址列表
    public void registerAllocatedIPs(List<String> ips) {
        for (String ip : ips) {
            registerAllocatedIP(ip);
        }
    }

    // 按顺序分配 IP 地址
    public Optional<String> allocateIP() {
        String[] allIps = this.subnetUtils.getInfo().getAllAddresses();
        for (String ip : allIps) {
            if (!this.allocatedIPs.contains(ip)) {
                this.allocatedIPs.add(ip);
                return Optional.ofNullable(ip);
            }
        }
        return Optional.empty(); // 没有可用的 IP 地址
    }

    // 释放 IP 地址
    public void releaseIP(String ip) {
        this.allocatedIPs.remove(ip);
    }


}
