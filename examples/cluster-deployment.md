# 集群部署示例

## 概述

本示例展示如何部署一个 3 节点的 WireGuard 注册中心集群。

## 环境准备

### 1. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE wireguard_registry;
CREATE USER 'wg_user'@'%' IDENTIFIED BY 'wg_password';
GRANT ALL PRIVILEGES ON wireguard_registry.* TO 'wg_user'@'%';
FLUSH PRIVILEGES;
```

### 2. 负载均衡器配置

#### Nginx 配置示例
```nginx
upstream wireguard_registry {
    server 192.168.1.10:8080;
    server 192.168.1.11:8080;
    server 192.168.1.12:8080;
}

server {
    listen 80;
    server_name wg-registry.example.com;
    
    location / {
        proxy_pass http://wireguard_registry;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 节点部署

### 节点 1 (192.168.1.10)

```yaml
# application.yml
server:
  port: 8080

spring:
  profiles:
    active: mysql
  datasource:
    url: jdbc:mysql://192.168.1.100:3306/wireguard_registry?useSSL=false&serverTimezone=UTC
    username: wg_user
    password: wg_password

wireguard:
  registry:
    mode: cluster
    node-id: "node-001"
```

### 节点 2 (192.168.1.11)

```yaml
# application.yml
server:
  port: 8080

spring:
  profiles:
    active: mysql
  datasource:
    url: jdbc:mysql://192.168.1.100:3306/wireguard_registry?useSSL=false&serverTimezone=UTC
    username: wg_user
    password: wg_password

wireguard:
  registry:
    mode: cluster
    node-id: "node-002"
```

### 节点 3 (192.168.1.12)

```yaml
# application.yml
server:
  port: 8080

spring:
  profiles:
    active: mysql
  datasource:
    url: jdbc:mysql://192.168.1.100:3306/wireguard_registry?useSSL=false&serverTimezone=UTC
    username: wg_user
    password: wg_password

wireguard:
  registry:
    mode: cluster
    node-id: "node-003"
```

## 启动节点

```bash
# 节点 1
java -jar wireguard-registry-service.jar

# 节点 2
java -jar wireguard-registry-service.jar

# 节点 3
java -jar wireguard-registry-service.jar
```

## 验证集群

### 1. 健康检查

```bash
# 检查各节点状态
curl http://192.168.1.10:8080/actuator/health
curl http://192.168.1.11:8080/actuator/health
curl http://192.168.1.12:8080/actuator/health
```

### 2. 功能测试

```bash
# 通过负载均衡器测试
curl http://wg-registry.example.com/v1/admin/info

# 注册网络节点
curl -X POST http://wg-registry.example.com/v1/registry/nodes \
  -H "Content-Type: application/json" \
  -d '{
    "network_id": "test-network",
    "node_id": "test-node",
    "public_key": "...",
    "signature": "..."
  }'

# 获取配置
curl http://wg-registry.example.com/v1/registry/networks/test-network/nodes/test-node/config
```

### 3. 数据一致性验证

```bash
# 在不同节点查询相同数据，应该返回一致结果
curl http://192.168.1.10:8080/v1/admin/networks
curl http://192.168.1.11:8080/v1/admin/networks
curl http://192.168.1.12:8080/v1/admin/networks
```

## 扩展节点

### 添加第 4 个节点

1. 创建相同的配置文件（修改 node-id 为 "node-004"）
2. 启动服务
3. 更新负载均衡器配置

```nginx
upstream wireguard_registry {
    server 192.168.1.10:8080;
    server 192.168.1.11:8080;
    server 192.168.1.12:8080;
    server 192.168.1.13:8080;  # 新增节点
}
```

4. 重新加载 Nginx 配置

```bash
nginx -s reload
```

## Docker 部署

### docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: wireguard_registry
      MYSQL_USER: wg_user
      MYSQL_PASSWORD: wg_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  registry-1:
    image: wireguard-registry:latest
    ports:
      - "8081:8080"
    environment:
      SPRING_PROFILES_ACTIVE: mysql
      WIREGUARD_REGISTRY_MODE: cluster
      WIREGUARD_REGISTRY_NODE_ID: node-001
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/wireguard_registry?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: wg_user
      SPRING_DATASOURCE_PASSWORD: wg_password
    depends_on:
      - mysql

  registry-2:
    image: wireguard-registry:latest
    ports:
      - "8082:8080"
    environment:
      SPRING_PROFILES_ACTIVE: mysql
      WIREGUARD_REGISTRY_MODE: cluster
      WIREGUARD_REGISTRY_NODE_ID: node-002
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/wireguard_registry?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: wg_user
      SPRING_DATASOURCE_PASSWORD: wg_password
    depends_on:
      - mysql

  registry-3:
    image: wireguard-registry:latest
    ports:
      - "8083:8080"
    environment:
      SPRING_PROFILES_ACTIVE: mysql
      WIREGUARD_REGISTRY_MODE: cluster
      WIREGUARD_REGISTRY_NODE_ID: node-003
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/wireguard_registry?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: wg_user
      SPRING_DATASOURCE_PASSWORD: wg_password
    depends_on:
      - mysql

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - registry-1
      - registry-2
      - registry-3

volumes:
  mysql_data:
```

### 启动集群

```bash
docker-compose up -d
```

## 监控

### 应用监控

```bash
# 查看应用日志
docker-compose logs -f registry-1
docker-compose logs -f registry-2
docker-compose logs -f registry-3
```

### 数据库监控

```bash
# 查看数据库连接数
docker-compose exec mysql mysql -u root -p -e "SHOW PROCESSLIST;"
```

## 故障处理

### 节点故障

节点故障时，负载均衡器会自动剔除故障节点，其他节点继续提供服务。

### 数据库故障

数据库故障时，所有节点都会受到影响。需要：
1. 修复数据库
2. 重启应用节点

### 网络分区

由于没有节点间通信，网络分区只影响节点与数据库的连接。

## 注意事项

1. **数据库高可用**: 生产环境建议使用 MySQL 主从复制或集群
2. **连接池配置**: 合理配置数据库连接池大小
3. **负载均衡**: 使用健康检查确保流量只转发到健康节点
4. **监控告警**: 监控节点状态和数据库状态
5. **备份策略**: 定期备份数据库数据