package icu.debug.net.wg.core;

import icu.debug.net.wg.core.model.config.WireGuardDefaultConfiguration;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.config.WireGuardInterface;
import icu.debug.net.wg.core.model.config.WireGuardPeer;
import icu.debug.net.wg.core.model.network.*;

import java.util.*;

public class WireGuardConfigGenerator {

    private final WireGuardDefaultConfiguration defaultConfiguration = new WireGuardDefaultConfiguration();

    private final WireGuardNetworkStruct networkStruct;

    private final Map<String, NetworkNodeWrapper> nodeWrapperMap = new HashMap<>();

    public WireGuardConfigGenerator(WireGuardNetworkStruct networkStruct) {
        this.networkStruct = networkStruct;
        this.mergeDefaultConfiguration(networkStruct.getNetworkNodes());
        this.initNodeWrapperMap(networkStruct);

    }

    /**
     * 合并默认配置
     *
     * @param networkNodes
     */
    private void mergeDefaultConfiguration(List<WireGuardNetworkNode> networkNodes) {

    }

    private void initNodeWrapperMap(WireGuardNetworkStruct networkStruct) {
        for (LocalAreaNetwork localAreaNetwork : networkStruct.getLocalAreaNetworks()) {
            for (int i = 0; i < localAreaNetwork.getNetworkNodes().size(); i++) {
                WireGuardNetworkNode networkNode = localAreaNetwork.getNetworkNodes().get(i);
                String hostname = networkNode.getServerNode().getHostname();
                if (nodeWrapperMap.containsKey(hostname)) {
                    throw new IllegalArgumentException("hostname: " + hostname + " is duplicate");
                }
                NetworkNodeWrapper nodeWrapper = new NetworkNodeWrapper();
                nodeWrapper.setNode(networkNode);
                nodeWrapper.setNetworkType(localAreaNetwork.getNetworkType());
                nodeWrapper.setLocalAreaNetwork(localAreaNetwork.getName());
                nodeWrapper.setBridge(isBridge(localAreaNetwork, i));
                nodeWrapperMap.put(hostname, nodeWrapper);
            }
        }
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
        if (!this.nodeWrapperMap.containsKey(hostname)) {
            throw new IllegalArgumentException("hostname: " + hostname + " is not exist");
        }
        NetworkNodeWrapper requestWrapper = this.nodeWrapperMap.get(hostname);
        List<WireGuardPeer> peers = new ArrayList<>();
        nodeWrapperMap.forEach((key, networkNodeWrapper) -> {
            peers.add(networkNodeWrapper.buildPeer(requestWrapper));
        });
        String name = this.networkStruct.getName();
        WireGuardInterface wgInterface = requestWrapper.toInterface();
        return new WireGuardIniConfig(name, wgInterface, peers);
    }

}
