package icu.debug.net.wg.service.controller;

import icu.debug.net.wg.core.storage.model.GeneratedConfig;
import icu.debug.net.wg.core.storage.model.StoredNetwork;
import icu.debug.net.wg.service.entity.ApiResponse;
import icu.debug.net.wg.service.entity.CreateNetworkRequest;
import icu.debug.net.wg.service.entity.NetworkListResponse;
import icu.debug.net.wg.service.service.NetworkConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 网络管理 API
 *
 * @author WireGuard Registry Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/networks")
public class NetworkController {

    private final NetworkConfigService networkService;

    @Autowired
    public NetworkController(NetworkConfigService networkService) {
        this.networkService = networkService;
    }

    /**
     * 创建网络
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StoredNetwork>> createNetwork(
            @RequestBody CreateNetworkRequest request) {
        log.info("Creating network: {}", request.getId());

        StoredNetwork network = networkService.createNetwork(
            request.getId(),
            request.getName(),
            request.getStruct(),
            request.getProperties()
        );

        return ResponseEntity.ok(ApiResponse.success(network));
    }

    /**
     * 获取网络列表 (AMIS CRUD 格式)
     */
    @GetMapping
    public ResponseEntity<NetworkListResponse> listNetworks() {
        log.info("Listing networks");

        List<StoredNetwork> networks = networkService.listNetworks();
        return ResponseEntity.ok(NetworkListResponse.success(networks));
    }

    /**
     * 获取网络详情
     */
    @GetMapping("/{networkId}")
    public ResponseEntity<ApiResponse<StoredNetwork>> getNetwork(
            @PathVariable String networkId) {
        log.info("Getting network: {}", networkId);

        StoredNetwork network = networkService.getNetwork(networkId);
        return ResponseEntity.ok(ApiResponse.success(network));
    }

    /**
     * 更新网络
     */
    @PutMapping("/{networkId}")
    public ResponseEntity<ApiResponse<StoredNetwork>> updateNetwork(
            @PathVariable String networkId,
            @RequestBody CreateNetworkRequest request) {
        log.info("Updating network: {}", networkId);

        StoredNetwork network = networkService.updateNetwork(
            networkId,
            request.getStruct(),
            request.getProperties()
        );

        return ResponseEntity.ok(ApiResponse.success(network));
    }

    /**
     * 删除网络
     */
    @DeleteMapping("/{networkId}")
    public ResponseEntity<ApiResponse<Void>> deleteNetwork(
            @PathVariable String networkId) {
        log.info("Deleting network: {}", networkId);

        networkService.deleteNetwork(networkId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 获取网络所有配置 (AMIS 格式)
     */
    @GetMapping("/{networkId}/configs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNetworkConfigs(
            @PathVariable String networkId) {
        log.info("Getting configs for network: {}", networkId);

        Map<String, GeneratedConfig> configs = networkService.getNetworkConfigs(networkId);

        // 转换为 AMIS 需要的格式
        List<GeneratedConfig> items = new ArrayList<>(configs.values());
        Map<String, Object> result = Map.of("items", items);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取指定节点配置
     */
    @GetMapping("/{networkId}/configs/{nodeId}")
    public ResponseEntity<ApiResponse<GeneratedConfig>> getNodeConfig(
            @PathVariable String networkId,
            @PathVariable String nodeId) {
        log.info("Getting config: network={}, node={}", networkId, nodeId);

        GeneratedConfig config = networkService.getNodeConfig(networkId, nodeId);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    /**
     * 下载节点配置文件
     */
    @GetMapping("/{networkId}/configs/{nodeId}/download")
    public ResponseEntity<String> downloadNodeConfig(
            @PathVariable String networkId,
            @PathVariable String nodeId) {
        log.info("Downloading config: network={}, node={}", networkId, nodeId);

        GeneratedConfig config = networkService.getNodeConfig(networkId, nodeId);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8")
            .header(HttpHeaders.CONTENT_DISPOSITION,
                   "attachment; filename=\"" + nodeId + ".conf\"")
            .body(config.getConfigText());
    }

    /**
     * 重新生成配置
     */
    @PostMapping("/{networkId}/regenerate")
    public ResponseEntity<ApiResponse<StoredNetwork>> regenerateConfigs(
            @PathVariable String networkId) {
        log.info("Regenerating configs for network: {}", networkId);

        StoredNetwork network = networkService.regenerateConfigs(networkId);
        return ResponseEntity.ok(ApiResponse.success(network));
    }

    /**
     * 获取配置版本
     */
    @GetMapping("/{networkId}/version")
    public ResponseEntity<ApiResponse<Long>> getNetworkVersion(
            @PathVariable String networkId) {
        log.info("Getting version for network: {}", networkId);

        StoredNetwork network = networkService.getNetwork(networkId);
        return ResponseEntity.ok(ApiResponse.success(network.getVersion()));
    }
}
