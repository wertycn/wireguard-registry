# WireGuard 注册中心和配置存储系统

## 概述

本系统实现了 WireGuard 配置的注册中心模式，支持：
- 多种存储后端（MySQL、MongoDB、SQLite、内存）
- 节点自动注册和配置分发
- 配置变更实时通知
- 心跳检测和节点状态管理
- REST API 和客户端 SDK

## 功能特性

### ✅ 配置存储
- **接口设计**: 统一的 `ConfigStorage` 接口
- **多存储支持**: MySQL、MongoDB、SQLite、内存存储
- **数据持久化**: 节点配置和生成的 WireGuard 配置
- **版本管理**: 配置版本跟踪和变更检测

### ✅ 注册中心模式
- **节点注册**: 自动注册网络节点
- **配置分发**: 中心统一生成和分发配置
- **变更通知**: 实时推送配置变更
- **心跳检测**: 监控节点在线状态
- **自动恢复**: 节点重新上线时自动同步配置

## 架构设计

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client SDK    │    │   REST API      │    │  Config Storage │
│                 │    │                 │    │                 │
│ - 节点注册      │◄──►│ - 注册管理      │◄──►│ - MySQL         │
│ - 配置订阅      │    │ - 配置分发      │    │ - MongoDB       │
│ - 心跳发送      │    │ - 状态监控      │    │ - SQLite        │
│ - 变更通知      │    │ - 网络管理      │    │ - Memory        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                │
                    ┌─────────────────┐
                    │ Registry Core   │
                    │                 │
                    │ - 配置生成      │
                    │ - 事件处理      │
                    │ - 版本管理      │
                    │ - 监听器管理    │
                    └─────────────────┘
```

## 快速开始

### 1. 启动注册中心服务

```bash
# 使用内存存储（默认）
java -jar wireguard-registry-service.jar

# 使用MySQL存储
java -jar wireguard-registry-service.jar --spring.profiles.active=mysql

# 使用MongoDB存储
java -jar wireguard-registry-service.jar --spring.profiles.active=mongodb

# 使用SQLite存储
java -jar wireguard-registry-service.jar --spring.profiles.active=sqlite
```

### 2. 客户端使用示例

```java
// 创建客户端
RegistryClient client = new RegistryClient("http://localhost:8080");

// 创建节点
WireGuardNetworkNode node = createNode();

// 注册节点
client.registerNode("my-network", node);

// 订阅配置变更
client.subscribeConfigChange("my-network", "my-node", new ConfigChangeListener() {
    @Override
    public void onConfigChanged(String networkId, String nodeId, 
                               WireGuardIniConfig oldConfig, 
                               WireGuardIniConfig newConfig, 
                               long version) {
        // 应用新配置
        applyConfig(newConfig);
    }
    
    @Override
    public void onNodeOffline(String networkId, String nodeId) {
        // 处理节点离线
    }
    
    @Override
    public void onNetworkDeleted(String networkId) {
        // 处理网络删除
    }
});

// 启动心跳
client.startHeartbeat("my-network", "my-node");

// 获取配置
WireGuardIniConfig config = client.getNodeConfig("my-network", "my-node").join();
```

## REST API 参考

### 节点管理

#### 注册节点
```http
POST /v1/registry/networks/{networkId}/nodes
Content-Type: application/json

{
  "node": {
    "serverNode": {
      "name": "node-1",
      "endpoint": "192.168.1.100:51820"
    },
    "address": "10.0.0.2/24",
    "listenPort": 51820,
    "privateKey": "...",
    "publicKey": "..."
  }
}
```

#### 获取节点配置
```http
GET /v1/registry/networks/{networkId}/nodes/{nodeId}/config
```

#### 发送心跳
```http
POST /v1/registry/networks/{networkId}/nodes/{nodeId}/heartbeat
Content-Type: application/json

{
  "online": true
}
```

#### 注销节点
```http
DELETE /v1/registry/networks/{networkId}/nodes/{nodeId}
```

### 网络管理

#### 获取网络版本
```http
GET /v1/registry/networks/{networkId}/version
```

#### 获取网络配置
```http
GET /v1/registry/networks/{networkId}/configs
```

#### 强制重新生成配置
```http
POST /v1/registry/networks/{networkId}/regenerate
```

#### 删除网络
```http
DELETE /v1/registry/networks/{networkId}
```

## 配置选项

### 应用配置 (application.yml)

```yaml
wireguard:
  registry:
    storage:
      type: memory # 支持: memory, mysql, mongodb, sqlite
    heartbeat:
      timeout: 30 # 心跳超时时间（秒）
      interval: 10 # 心跳间隔（秒）

# MySQL配置
spring:
  profiles: mysql
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wireguard_registry
    username: root
    password: password

# MongoDB配置
spring:
  profiles: mongodb
  data:
    mongodb:
      host: localhost
      port: 27017
      database: wireguard_registry
      username: admin
      password: password
```

### 客户端配置

```java
RegistryClient client = new RegistryClient("http://localhost:8080");

// 设置心跳间隔
client.setHeartbeatInterval(15); // 秒

// 启动心跳
client.startHeartbeat("network-id", "node-id");
```

## 存储后端支持

### 1. 内存存储 (默认)
- **优点**: 快速、无需额外配置
- **缺点**: 数据不持久化
- **适用场景**: 开发测试、临时环境

### 2. MySQL 存储
- **优点**: 成熟稳定、支持事务
- **缺点**: 需要额外数据库服务
- **适用场景**: 生产环境、需要强一致性

### 3. MongoDB 存储
- **优点**: 灵活的文档结构、水平扩展
- **缺点**: 需要额外数据库服务
- **适用场景**: 大规模部署、需要高可用

### 4. SQLite 存储
- **优点**: 轻量级、无需额外服务
- **缺点**: 单机部署、并发限制
- **适用场景**: 小规模部署、嵌入式环境

## 部署建议

### 开发环境
```bash
# 使用内存存储
java -jar wireguard-registry-service.jar
```

### 生产环境
```bash
# 使用MySQL存储
java -jar wireguard-registry-service.jar \
  --spring.profiles.active=mysql \
  --spring.datasource.url=jdbc:mysql://mysql-server:3306/wireguard_registry \
  --spring.datasource.username=wireguard \
  --spring.datasource.password=secure_password
```

### Docker 部署
```dockerfile
FROM openjdk:17-jre-slim

COPY wireguard-registry-service.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  wireguard-registry:
    image: wireguard-registry:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=mysql
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/wireguard_registry
      - SPRING_DATASOURCE_USERNAME=wireguard
      - SPRING_DATASOURCE_PASSWORD=password
    depends_on:
      - mysql
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root_password
      - MYSQL_DATABASE=wireguard_registry
      - MYSQL_USER=wireguard
      - MYSQL_PASSWORD=password
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## 监控和日志

### 健康检查
```http
GET /actuator/health
```

### 监控指标
```http
GET /actuator/metrics
```

### 日志配置
```yaml
logging:
  level:
    icu.debug.net.wg: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 故障排除

### 常见问题

1. **节点注册失败**
   - 检查网络连接
   - 验证请求格式
   - 查看服务器日志

2. **配置变更未生效**
   - 确认心跳正常
   - 检查网络版本
   - 验证监听器配置

3. **数据库连接失败**
   - 检查数据库服务状态
   - 验证连接配置
   - 确认权限设置

### 日志分析
```bash
# 查看注册相关日志
grep "Registering node" application.log

# 查看配置变更日志
grep "Config changed" application.log

# 查看心跳日志
grep "Heartbeat" application.log
```

## 扩展开发

### 自定义存储实现
```java
@Component
public class CustomConfigStorage implements ConfigStorage {
    // 实现接口方法
}
```

### 自定义配置监听器
```java
@Component
public class CustomConfigChangeListener implements ConfigChangeListener {
    // 实现接口方法
}
```

### 添加新的REST端点
```java
@RestController
@RequestMapping("/v1/custom")
public class CustomController {
    // 自定义API
}
```

## 安全考虑

1. **API 认证**: 建议添加 JWT 或 OAuth2 认证
2. **数据加密**: 敏感数据加密存储
3. **网络安全**: 使用 HTTPS 和防火墙
4. **权限控制**: 实现细粒度权限管理

## 性能优化

1. **连接池配置**: 优化数据库连接池
2. **缓存策略**: 添加 Redis 缓存
3. **异步处理**: 使用异步任务处理
4. **负载均衡**: 多实例部署

## 总结

本系统实现了完整的 WireGuard 配置注册中心功能，包括：

- ✅ 多种存储后端支持
- ✅ 节点自动注册和配置分发
- ✅ 实时配置变更通知
- ✅ 心跳检测和状态管理
- ✅ 完整的 REST API
- ✅ 易用的客户端 SDK

系统采用模块化设计，易于扩展和维护，适合各种规模的部署场景。