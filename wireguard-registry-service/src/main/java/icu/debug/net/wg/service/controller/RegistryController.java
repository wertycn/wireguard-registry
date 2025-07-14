package icu.debug.net.wg.service.controller;

import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import icu.debug.net.wg.core.registry.ConfigRegistry;
import icu.debug.net.wg.service.entity.HttpResult;
import icu.debug.net.wg.service.entity.NodeRegistrationRequest;
import icu.debug.net.wg.service.entity.NodeStatusRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 注册中心REST API控制器
 */
@RestController
@RequestMapping("/v1/registry")
@Slf4j
public class RegistryController {

    private final ConfigRegistry configRegistry;

    public RegistryController(ConfigRegistry configRegistry) {
        this.configRegistry = configRegistry;
    }

    /**
     * 注册节点
     */
    @PostMapping("/networks/{networkId}/nodes")
    public HttpResult<Void> registerNode(@PathVariable String networkId, 
                                        @RequestBody NodeRegistrationRequest request) {
        log.info("Registering node {} in network {}", request.getNode().getServerNode().getHostname(), networkId);
        configRegistry.registerNode(networkId, request.getNode());
        return HttpResult.success();
    }

    /**
     * 注销节点
     */
    @DeleteMapping("/networks/{networkId}/nodes/{nodeId}")
    public HttpResult<Void> unregisterNode(@PathVariable String networkId, 
                                          @PathVariable String nodeId) {
        log.info("Unregistering node {} from network {}", nodeId, networkId);
        configRegistry.unregisterNode(networkId, nodeId);
        return HttpResult.success();
    }

    /**
     * 获取节点信息
     */
    @GetMapping("/networks/{networkId}/nodes/{nodeId}")
    public HttpResult<WireGuardNetworkNode> getNode(@PathVariable String networkId, 
                                                   @PathVariable String nodeId) {
        WireGuardNetworkNode node = configRegistry.getNode(networkId, nodeId);
        if (node == null) {
            return HttpResult.error("Node not found");
        }
        return HttpResult.success(node);
    }

    /**
     * 获取网络下的所有节点
     */
    @GetMapping("/networks/{networkId}/nodes")
    public HttpResult<List<WireGuardNetworkNode>> getNodes(@PathVariable String networkId) {
        List<WireGuardNetworkNode> nodes = configRegistry.getNodes(networkId);
        return HttpResult.success(nodes);
    }

    /**
     * 获取节点配置
     */
    @GetMapping("/networks/{networkId}/nodes/{nodeId}/config")
    public HttpResult<WireGuardIniConfig> getNodeConfig(@PathVariable String networkId, 
                                                       @PathVariable String nodeId) {
        WireGuardIniConfig config = configRegistry.getNodeConfig(networkId, nodeId);
        if (config == null) {
            return HttpResult.error("Config not found");
        }
        return HttpResult.success(config);
    }

    /**
     * 获取网络的所有配置
     */
    @GetMapping("/networks/{networkId}/configs")
    public HttpResult<Map<String, WireGuardIniConfig>> getNetworkConfigs(@PathVariable String networkId) {
        Map<String, WireGuardIniConfig> configs = configRegistry.getNetworkConfigs(networkId);
        return HttpResult.success(configs);
    }

    /**
     * 强制重新生成配置
     */
    @PostMapping("/networks/{networkId}/regenerate")
    public HttpResult<Void> regenerateConfig(@PathVariable String networkId) {
        log.info("Regenerating config for network {}", networkId);
        configRegistry.generateAndDistributeConfig(networkId);
        return HttpResult.success();
    }

    /**
     * 获取网络版本
     */
    @GetMapping("/networks/{networkId}/version")
    public HttpResult<Long> getNetworkVersion(@PathVariable String networkId) {
        long version = configRegistry.getNetworkVersion(networkId);
        return HttpResult.success(version);
    }

    /**
     * 更新节点状态（心跳）
     */
    @PostMapping("/networks/{networkId}/nodes/{nodeId}/heartbeat")
    public HttpResult<Void> updateNodeStatus(@PathVariable String networkId, 
                                            @PathVariable String nodeId,
                                            @RequestBody NodeStatusRequest request) {
        configRegistry.updateNodeStatus(networkId, nodeId, request.isOnline());
        return HttpResult.success();
    }

    /**
     * 检查节点是否在线
     */
    @GetMapping("/networks/{networkId}/nodes/{nodeId}/status")
    public HttpResult<Boolean> isNodeOnline(@PathVariable String networkId, 
                                           @PathVariable String nodeId) {
        boolean online = configRegistry.isNodeOnline(networkId, nodeId);
        return HttpResult.success(online);
    }

    /**
     * 获取所有网络ID
     */
    @GetMapping("/networks")
    public HttpResult<List<String>> getAllNetworkIds() {
        List<String> networkIds = configRegistry.getAllNetworkIds();
        return HttpResult.success(networkIds);
    }

    /**
     * 删除网络
     */
    @DeleteMapping("/networks/{networkId}")
    public HttpResult<Void> deleteNetwork(@PathVariable String networkId) {
        log.info("Deleting network {}", networkId);
        configRegistry.deleteNetwork(networkId);
        return HttpResult.success();
    }
}