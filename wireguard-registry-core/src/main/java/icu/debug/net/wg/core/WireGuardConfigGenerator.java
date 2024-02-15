package icu.debug.net.wg.core;

import icu.debug.net.wg.core.helper.WireGuardGenKeyHelper;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.config.WireGuardInterface;
import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.config.WireGuardPeer;
import icu.debug.net.wg.core.model.network.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class WireGuardConfigGenerator {

    // TODO: 默认配置必需属性校验
    private final WireGuardNetProperties defaultConfiguration;

    private final WireGuardNetworkStruct networkStruct;

    private final Map<String, NetworkNodeWrapper> nodeWrapperMap = new HashMap<>();

    private final NetAddressAllocator netAddressAllocator;

    public WireGuardConfigGenerator(WireGuardNetworkStruct struct, WireGuardNetProperties defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
        this.networkStruct = this.mergeSubNetProp(struct);
        this.netAddressAllocator = new NetAddressAllocator(this.networkStruct.getAddress(), this.networkStruct.getNetmask());
        this.registryUsedSubNetAddress(this.networkStruct);
        // 全局默认配置设置
        this.appendDefaultProperties();
        this.initNodeWrapperMap(this.networkStruct);
    }

    private void appendDefaultProperties() {
        this.networkStruct.getLocalAreaNetworks().stream()
                .filter(item -> item.getNetworkNodes() != null)
                .map(LocalAreaNetwork::getNetworkNodes)
                .flatMap(List::stream)
                .forEach(this::appendDefaultProperties);

    }

    private void appendDefaultProperties(WireGuardNetworkNode node) {
        if (node.isIgnoreDefault()) {
            log.info("WireGuard Struct node [{}] ignore default properties", node);
            return;
        }
        // 子网分配
        handlerSubNetAllocate(node);

        // 处理密钥分配
        handlerKeyAllecte(node);

        // 默认属性配置
        handlerDefaultBaseProp(node);
    }

    private void handlerSubNetAllocate(WireGuardNetworkNode node) {
        if (!StringUtils.hasLength(node.getAddress())) {
            log.debug("node [hostname={}] not set sub net address , auto allocate sub net address", node.getServerNode().getHostname());
            allocateSubNetAddress(node);
            return;
        }
        if (!netAddressAllocator.isInSubnet(node.getAddress())) {
            log.warn("node [hostname={}] custom net address not valid , auto allocate new sub net address", node.getServerNode().getHostname());
            allocateSubNetAddress(node);
            return;
        }
        log.debug("node [hostname={}] use custom net address {}", node.getServerNode().getHostname(), node.getAddress());
    }

    private void allocateSubNetAddress(WireGuardNetworkNode node) {
        netAddressAllocator.allocateIP().ifPresentOrElse(node::setAddress, () -> {
            throw new IllegalArgumentException("WireGuard Struct node address is empty , and can not allocate address");
        });
    }

    private void handlerDefaultBaseProp(WireGuardNetworkNode node) {
        if (node.getListenPort() == null) {
            node.setListenPort(defaultConfiguration.getListenPort());
        }
        if (CollectionUtils.isEmpty(node.getDns())) {
            node.setDns(defaultConfiguration.getDns());
        }
        if (!StringUtils.hasLength(node.getTable())) {
            node.setTable(defaultConfiguration.getTable());
        }
        if (node.getMtu() == null) {
            node.setMtu(defaultConfiguration.getMtu());
        }
    }

    private static void handlerKeyAllecte(WireGuardNetworkNode node) {
        // 密钥分配
        if (!StringUtils.hasLength(node.getPrivateKey()) || !WireGuardGenKeyHelper.formatValid(node.getPrivateKey())) {
            String privateKey = WireGuardGenKeyHelper.genPrivateKey();
            node.setPrivateKey(privateKey);
            node.setPublicKey(WireGuardGenKeyHelper.genPubKeyByPrivateKey(privateKey));
        }

        String publicKey = node.getPublicKey();
        boolean formatNotValid = !StringUtils.hasLength(publicKey) || !WireGuardGenKeyHelper.formatValid(publicKey);
        if (formatNotValid || !WireGuardGenKeyHelper.verify(node.getPrivateKey(), publicKey)) {
            log.info("WireGuard Struct node [{}] public key is empty or not valid, regenerate private and public key", node);
            String privateKey = WireGuardGenKeyHelper.genPrivateKey();
            node.setPrivateKey(privateKey);
            node.setPublicKey(WireGuardGenKeyHelper.genPubKeyByPrivateKey(privateKey));
        }
    }

    /**
     * 合并子网默认配置
     *
     * @param networkStruct
     */
    private WireGuardNetworkStruct mergeSubNetProp(WireGuardNetworkStruct networkStruct) {
        // 子网分配
        if (!StringUtils.hasLength(networkStruct.getAddress())) {
            log.info("WireGuard Struct Address is empty , use default address {}", defaultConfiguration.getAddress());
            networkStruct.setAddress(defaultConfiguration.getAddress());
        }
        if (!StringUtils.hasLength(networkStruct.getNetmask())) {
            log.info("WireGuard Struct Netmask is empty , use default address {}", defaultConfiguration.getAddress());
            networkStruct.setNetmask(defaultConfiguration.getNetmask());
        }
        return networkStruct;


    }

    /**
     * 注册已使用的内网地址
     *
     * @param networkStruct
     */
    private void registryUsedSubNetAddress(WireGuardNetworkStruct networkStruct) {
        // 获取已配置的所有子网地址
        List<String> usedAddress = getUsedAddress(networkStruct);
        log.info("WireGuard Struct already used address [{}]", String.join(",", usedAddress));
        netAddressAllocator.registerAllocatedIPs(usedAddress);
    }

    private static List<String> getUsedAddress(WireGuardNetworkStruct networkStruct) {
        return networkStruct.getLocalAreaNetworks()
                .stream()
                .filter(item -> item.getNetworkNodes() != null)
                .map(LocalAreaNetwork::getNetworkNodes)
                .flatMap(List::stream)
                .map(WireGuardNetworkNode::getAddress)
                .filter(StringUtils::hasLength).toList();
    }

    private void initNodeWrapperMap(WireGuardNetworkStruct networkStruct) {
        for (LocalAreaNetwork localAreaNetwork : networkStruct.getLocalAreaNetworks()) {
            List<WireGuardNetworkNode> networkNodes = localAreaNetwork.getNetworkNodes();
            if (ObjectUtils.isEmpty(networkNodes)) {
                continue;
            }
            for (int i = 0; i < networkNodes.size(); i++) {
                WireGuardNetworkNode networkNode = networkNodes.get(i);
                String hostname = networkNode.getServerNode().getHostname();
                // 校验节点名称
                Assert.hasLength(hostname, "hostname it must has length");
                Assert.isTrue(!nodeWrapperMap.containsKey(hostname), "hostname: " + hostname + " is duplicate");
                NetworkNodeWrapper nodeWrapper = buildNodeWrapper(localAreaNetwork, i, networkNode);
                nodeWrapperMap.put(hostname, nodeWrapper);
            }
        }
    }

    private static NetworkNodeWrapper buildNodeWrapper(LocalAreaNetwork localAreaNetwork, int i, WireGuardNetworkNode networkNode) {
        NetworkNodeWrapper nodeWrapper = new NetworkNodeWrapper();
        nodeWrapper.setNode(networkNode);
        nodeWrapper.setNetworkType(localAreaNetwork.getNetworkType());
        nodeWrapper.setLocalAreaNetwork(localAreaNetwork.getName());
        nodeWrapper.setBridge(isBridge(localAreaNetwork, i));
        return nodeWrapper;
    }

    /**
     * BRIDGE_NAT的第一个节点，视为中继节点
     *
     * @param lanName 局域网名词
     * @param i       节点编号
     * @return
     */
    private static boolean isBridge(LocalAreaNetwork lanName, int i) {
        return i == 0 && lanName.getNetworkType() == NetworkType.BRIDGE_NAT;
    }

    public WireGuardIniConfig buildWireGuardIniConfig(String hostname) {
        return buildConfig(hostname).getConfig();
    }

    public NetworkNodeConfigWrapper buildConfig(String hostname) {
        Assert.isTrue(this.nodeWrapperMap.containsKey(hostname), "hostname: " + hostname + " is not exist");
        NetworkNodeWrapper requestWrapper = this.nodeWrapperMap.get(hostname);
        List<WireGuardPeer> peers = new ArrayList<>();
        nodeWrapperMap.forEach((key, value) -> peers.add(value.buildPeer(requestWrapper)));
        String name = this.networkStruct.getName();
        WireGuardInterface wgInterface = requestWrapper.toInterface();
        WireGuardIniConfig config = new WireGuardIniConfig(name, wgInterface, peers);
        return new NetworkNodeConfigWrapper(requestWrapper.getNode(), config);
    }

    public List<NetworkNodeConfigWrapper> buildWireGuardIniConfigs() {
        List<NetworkNodeConfigWrapper> result = new ArrayList<>();
        nodeWrapperMap.forEach((key, value) -> result.add(buildConfig(key)));
        return result;
    }

    public Map<String, WireGuardIniConfig> buildWireGuardIniConfigMap() {
        return buildWireGuardIniConfigs()
                .stream()
                .collect(Collectors.toMap(NetworkNodeConfigWrapper::getHostName, NetworkNodeConfigWrapper::getConfig));
    }

}
