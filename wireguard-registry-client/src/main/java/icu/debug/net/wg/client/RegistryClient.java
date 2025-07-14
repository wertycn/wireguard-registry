package icu.debug.net.wg.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import icu.debug.net.wg.core.registry.ConfigChangeListener;
import icu.debug.net.wg.service.entity.HttpResult;
import icu.debug.net.wg.service.entity.NodeRegistrationRequest;
import icu.debug.net.wg.service.entity.NodeStatusRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 注册中心客户端SDK
 */
@Slf4j
public class RegistryClient {

    private final String serverUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    
    // 配置变更监听器
    private ConfigChangeListener configChangeListener;
    private String networkId;
    private String nodeId;
    
    // 心跳相关
    private boolean heartbeatEnabled = false;
    private long heartbeatInterval = 10; // 秒
    
    public RegistryClient(String serverUrl) {
        this.serverUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        this.httpClient = HttpClient.newBuilder()
                .timeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    /**
     * 注册节点
     */
    public CompletableFuture<Void> registerNode(String networkId, WireGuardNetworkNode node) {
        return CompletableFuture.runAsync(() -> {
            try {
                String url = String.format("%s/v1/registry/networks/%s/nodes", serverUrl, networkId);
                NodeRegistrationRequest request = new NodeRegistrationRequest(node);
                String requestBody = objectMapper.writeValueAsString(request);
                
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Failed to register node: " + response.body());
                }
                
                log.info("Node {} registered successfully in network {}", node.getServerNode().getName(), networkId);
                
            } catch (Exception e) {
                log.error("Failed to register node", e);
                throw new RuntimeException("Failed to register node", e);
            }
        });
    }

    /**
     * 注销节点
     */
    public CompletableFuture<Void> unregisterNode(String networkId, String nodeId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String url = String.format("%s/v1/registry/networks/%s/nodes/%s", serverUrl, networkId, nodeId);
                
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .DELETE()
                        .build();
                
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Failed to unregister node: " + response.body());
                }
                
                log.info("Node {} unregistered successfully from network {}", nodeId, networkId);
                
            } catch (Exception e) {
                log.error("Failed to unregister node", e);
                throw new RuntimeException("Failed to unregister node", e);
            }
        });
    }

    /**
     * 获取节点配置
     */
    public CompletableFuture<WireGuardIniConfig> getNodeConfig(String networkId, String nodeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format("%s/v1/registry/networks/%s/nodes/%s/config", serverUrl, networkId, nodeId);
                
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Failed to get node config: " + response.body());
                }
                
                HttpResult<WireGuardIniConfig> result = objectMapper.readValue(response.body(), 
                        new TypeReference<HttpResult<WireGuardIniConfig>>() {});
                
                return result.getData();
                
            } catch (Exception e) {
                log.error("Failed to get node config", e);
                throw new RuntimeException("Failed to get node config", e);
            }
        });
    }

    /**
     * 获取网络版本
     */
    public CompletableFuture<Long> getNetworkVersion(String networkId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format("%s/v1/registry/networks/%s/version", serverUrl, networkId);
                
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Failed to get network version: " + response.body());
                }
                
                HttpResult<Long> result = objectMapper.readValue(response.body(), 
                        new TypeReference<HttpResult<Long>>() {});
                
                return result.getData();
                
            } catch (Exception e) {
                log.error("Failed to get network version", e);
                throw new RuntimeException("Failed to get network version", e);
            }
        });
    }

    /**
     * 发送心跳
     */
    public CompletableFuture<Void> sendHeartbeat(String networkId, String nodeId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String url = String.format("%s/v1/registry/networks/%s/nodes/%s/heartbeat", serverUrl, networkId, nodeId);
                NodeStatusRequest request = new NodeStatusRequest(true);
                String requestBody = objectMapper.writeValueAsString(request);
                
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    log.warn("Failed to send heartbeat: {}", response.body());
                }
                
            } catch (Exception e) {
                log.error("Failed to send heartbeat", e);
            }
        });
    }

    /**
     * 启动心跳
     */
    public void startHeartbeat(String networkId, String nodeId) {
        this.networkId = networkId;
        this.nodeId = nodeId;
        this.heartbeatEnabled = true;
        
        scheduler.scheduleAtFixedRate(() -> {
            if (heartbeatEnabled) {
                sendHeartbeat(networkId, nodeId);
            }
        }, 0, heartbeatInterval, TimeUnit.SECONDS);
        
        log.info("Heartbeat started for node {} in network {}", nodeId, networkId);
    }

    /**
     * 停止心跳
     */
    public void stopHeartbeat() {
        this.heartbeatEnabled = false;
        log.info("Heartbeat stopped for node {} in network {}", nodeId, networkId);
    }

    /**
     * 订阅配置变更
     */
    public void subscribeConfigChange(String networkId, String nodeId, ConfigChangeListener listener) {
        this.configChangeListener = listener;
        this.networkId = networkId;
        this.nodeId = nodeId;
        
        // 启动配置变更检查
        startConfigChangeCheck();
        
        log.info("Subscribed to config changes for node {} in network {}", nodeId, networkId);
    }

    /**
     * 启动配置变更检查
     */
    private void startConfigChangeCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkConfigChange();
            } catch (Exception e) {
                log.error("Error checking config change", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private Long lastKnownVersion = null;
    private WireGuardIniConfig lastKnownConfig = null;

    private void checkConfigChange() {
        if (configChangeListener == null || networkId == null || nodeId == null) {
            return;
        }
        
        try {
            // 检查版本变更
            Long currentVersion = getNetworkVersion(networkId).join();
            if (lastKnownVersion == null || !lastKnownVersion.equals(currentVersion)) {
                // 版本变更，获取新配置
                WireGuardIniConfig newConfig = getNodeConfig(networkId, nodeId).join();
                
                if (newConfig != null) {
                    // 通知配置变更
                    configChangeListener.onConfigChanged(networkId, nodeId, lastKnownConfig, newConfig, currentVersion);
                    
                    lastKnownVersion = currentVersion;
                    lastKnownConfig = newConfig;
                }
            }
        } catch (Exception e) {
            log.error("Error checking config change", e);
        }
    }

    /**
     * 获取所有网络ID
     */
    public CompletableFuture<List<String>> getAllNetworkIds() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format("%s/v1/registry/networks", serverUrl);
                
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Failed to get network IDs: " + response.body());
                }
                
                HttpResult<List<String>> result = objectMapper.readValue(response.body(), 
                        new TypeReference<HttpResult<List<String>>>() {});
                
                return result.getData();
                
            } catch (Exception e) {
                log.error("Failed to get network IDs", e);
                throw new RuntimeException("Failed to get network IDs", e);
            }
        });
    }

    /**
     * 关闭客户端
     */
    public void close() {
        stopHeartbeat();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    /**
     * 设置心跳间隔
     */
    public void setHeartbeatInterval(long interval) {
        this.heartbeatInterval = interval;
    }
}