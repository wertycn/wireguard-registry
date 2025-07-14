package icu.debug.net.wg.client.example;

import icu.debug.net.wg.client.RegistryClient;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import icu.debug.net.wg.core.model.network.ServerNode;
import icu.debug.net.wg.core.model.network.WireGuardNetworkNode;
import icu.debug.net.wg.core.registry.ConfigChangeListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端使用示例
 */
@Slf4j
public class ClientExample {

    public static void main(String[] args) {
        // 创建注册中心客户端
        RegistryClient client = new RegistryClient("http://localhost:8080");
        
        // 创建网络节点
        WireGuardNetworkNode node = createSampleNode();
        
        String networkId = "test-network";
        String nodeId = node.getServerNode().getHostname();
        
        try {
            // 注册节点
            log.info("Registering node...");
            client.registerNode(networkId, node).join();
            
            // 订阅配置变更
            client.subscribeConfigChange(networkId, nodeId, new ConfigChangeListener() {
                @Override
                public void onConfigChanged(String networkId, String nodeId, WireGuardIniConfig oldConfig, WireGuardIniConfig newConfig, long version) {
                    log.info("Config changed for node {} in network {}, version: {}", nodeId, networkId, version);
                    log.info("New config: {}", newConfig);
                    
                    // 在这里可以应用新的配置
                    // 例如：重新启动WireGuard服务
                    applyConfig(newConfig);
                }
                
                @Override
                public void onNodeOffline(String networkId, String nodeId) {
                    log.warn("Node {} in network {} is offline", nodeId, networkId);
                }
                
                @Override
                public void onNetworkDeleted(String networkId) {
                    log.warn("Network {} has been deleted", networkId);
                }
            });
            
            // 启动心跳
            client.startHeartbeat(networkId, nodeId);
            
            // 获取初始配置
            WireGuardIniConfig config = client.getNodeConfig(networkId, nodeId).join();
            log.info("Initial config: {}", config);
            
            // 应用配置
            applyConfig(config);
            
            // 保持运行
            log.info("Client is running... Press Ctrl+C to stop");
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down client...");
                try {
                    client.unregisterNode(networkId, nodeId).join();
                    client.close();
                } catch (Exception e) {
                    log.error("Error during shutdown", e);
                }
            }));
            
            // 等待中断
            Thread.currentThread().join();
            
        } catch (Exception e) {
            log.error("Error in client example", e);
        } finally {
            client.close();
        }
    }
    
    private static WireGuardNetworkNode createSampleNode() {
        WireGuardNetworkNode node = new WireGuardNetworkNode();
        
        // 设置服务器节点信息
        ServerNode serverNode = new ServerNode();
        serverNode.setHostname("client-node-1");
        serverNode.setPublicAddress("192.168.1.100");
        node.setServerNode(serverNode);
        
        // 设置WireGuard配置
        node.setAddress("10.0.0.2/24");
        node.setListenPort(51820);
        node.setPrivateKey("yAnz5TF+lXXJte14tji3zlMNq+hd2rYUIgJBgB3fBmk=");
        node.setPublicKey("xTIBA5rboUvnH4htodjb6e697QjLERt1NAB4mZqp8Dg=");
        
        return node;
    }
    
    private static void applyConfig(WireGuardIniConfig config) {
        if (config == null) {
            log.warn("Config is null, skipping application");
            return;
        }
        
        log.info("Applying WireGuard configuration:");
        log.info("Interface: {}", config.getWgInterface().getName());
        log.info("Address: {}", config.getWgInterface().getAddress());
        log.info("Listen Port: {}", config.getWgInterface().getListenPort());
        log.info("Peers: {}", config.getPeers().size());
        
        // 在实际应用中，这里会：
        // 1. 生成WireGuard配置文件
        // 2. 重新启动WireGuard服务
        // 3. 验证连接状态
        
        // 示例：生成配置文件内容
        String configContent = config.toIniString();
        log.info("Generated config content:\n{}", configContent);
        
        // 这里可以将配置写入文件或直接应用到系统
        // Files.writeString(Paths.get("/etc/wireguard/wg0.conf"), configContent);
        // Runtime.getRuntime().exec("systemctl restart wg-quick@wg0");
    }
}