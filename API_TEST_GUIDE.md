# WireGuard Registry API 测试指南

## 启动应用

### 1. 编译项目
```bash
cd /home/user/wireguard-registry
mvn clean package -DskipTests
```

### 2. 启动服务（内存模式）
```bash
cd wireguard-registry-service/target
java -jar wireguard-registry-service-0.0.1-SNAPSHOT.jar \
  --wireguard.storage.type=memory
```

或者设置环境变量：
```bash
export STORAGE_TYPE=memory
java -jar wireguard-registry-service-0.0.1-SNAPSHOT.jar
```

服务将在 http://localhost:8080 启动

## API 测试

### 测试 1: 创建网络

```bash
curl -X POST http://localhost:8080/api/v1/networks \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-network",
    "name": "测试网络",
    "struct": {
      "address": "10.0.0.1",
      "netmask": "255.255.255.0",
      "localAreaNetworks": [
        {
          "name": "lan1",
          "networkType": "POINT_TO_SITE",
          "networkNodes": [
            {
              "serverNode": {
                "hostname": "server1",
                "endpoint": {
                  "host": "1.2.3.4",
                  "port": 51820
                },
                "endpointType": "PUBLIC"
              }
            },
            {
              "serverNode": {
                "hostname": "client1",
                "endpointType": "PRIVATE"
              }
            }
          ]
        }
      ]
    },
    "properties": {
      "address": "10.0.0.1",
      "netmask": "255.255.255.0",
      "listen_port": 51820
    }
  }'
```

**期望响应**:
```json
{
  "status": 0,
  "msg": "success",
  "data": {
    "id": "test-network",
    "name": "测试网络",
    "version": 1,
    "createdAt": "2024-...",
    "updatedAt": "2024-..."
  }
}
```

### 测试 2: 获取网络列表

```bash
curl http://localhost:8080/api/v1/networks
```

**期望响应**:
```json
{
  "status": 0,
  "msg": "success",
  "items": [
    {
      "id": "test-network",
      "name": "测试网络",
      "version": 1,
      ...
    }
  ],
  "total": 1
}
```

### 测试 3: 获取网络详情

```bash
curl http://localhost:8080/api/v1/networks/test-network
```

### 测试 4: 获取网络所有配置

```bash
curl http://localhost:8080/api/v1/networks/test-network/configs
```

**期望响应**:
```json
{
  "status": 0,
  "msg": "success",
  "data": {
    "items": [
      {
        "networkId": "test-network",
        "nodeId": "server1",
        "nodeName": "server1",
        "configText": "[Interface]\n...",
        "version": 1
      },
      {
        "networkId": "test-network",
        "nodeId": "client1",
        ...
      }
    ]
  }
}
```

### 测试 5: 获取单个节点配置

```bash
curl http://localhost:8080/api/v1/networks/test-network/configs/server1
```

### 测试 6: 下载配置文件

```bash
curl http://localhost:8080/api/v1/networks/test-network/configs/server1/download \
  -o server1.conf
```

**检查下载的文件**:
```bash
cat server1.conf
```

应该看到类似的内容：
```ini
[Interface]
PrivateKey = ...
Address = 10.0.0.2/32
ListenPort = 51820

[Peer]
PublicKey = ...
AllowedIPs = 10.0.0.3/32
Endpoint = 1.2.3.4:51820
```

### 测试 7: 重新生成配置

```bash
curl -X POST http://localhost:8080/api/v1/networks/test-network/regenerate
```

### 测试 8: 删除网络

```bash
curl -X DELETE http://localhost:8080/api/v1/networks/test-network
```

### 测试 9: 验证删除

```bash
curl http://localhost:8080/api/v1/networks
```

应该返回空列表。

## 前端页面测试

### 1. 访问配置生成页面
```
http://localhost:8080?app=generate
```

### 2. 访问网络管理页面
```
http://localhost:8080?app=networks
```

### 3. 测试流程

1. **在配置生成页面创建配置**
   - 填写网络拓扑信息
   - 点击"生成配置（临时）"查看生成结果
   - 点击"保存为网络"保存到数据库

2. **在网络管理页面查看**
   - 应该能看到刚才保存的网络
   - 点击"查看配置"查看所有节点配置
   - 可以下载配置文件
   - 可以删除网络

## 验证存储功能

### 内存存储验证

1. 启动服务（默认内存模式）
2. 创建几个网络
3. 重启服务
4. 查询网络列表 - **应该为空**（内存不持久化）

### SQLite 存储验证

1. 启动服务（SQLite 模式）
```bash
java -jar wireguard-registry-service-0.0.1-SNAPSHOT.jar \
  --wireguard.storage.type=sqlite \
  --wireguard.storage.sqlite.url=jdbc:sqlite:./test.db
```

2. 创建几个网络
3. 重启服务（使用相同的数据库文件）
4. 查询网络列表 - **应该能看到之前的网络**（数据持久化）
5. 检查数据库文件
```bash
ls -lh test.db
sqlite3 test.db "SELECT * FROM wg_networks;"
```

## 故障排查

### 问题 1: 服务启动失败

检查日志：
```bash
tail -f logs/spring.log
```

常见原因：
- 端口 8080 被占用
- 存储配置错误
- 数据库文件权限问题

### 问题 2: API 返回 500 错误

检查日志查看详细错误信息。常见原因：
- 请求 JSON 格式错误
- 必填字段缺失
- 网络拓扑配置不合法

### 问题 3: 配置生成失败

可能原因：
- IP 地址分配冲突
- 网络拓扑结构不完整
- 缺少必要的节点信息

## 性能测试

### 批量创建网络
```bash
#!/bin/bash
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/v1/networks \
    -H "Content-Type: application/json" \
    -d "{
      \"id\": \"network-$i\",
      \"name\": \"Network $i\",
      \"struct\": {...},
      \"properties\": {...}
    }" &
done
wait
```

### 查询性能
```bash
time curl http://localhost:8080/api/v1/networks
```

## 日志级别调整

在 `application.yml` 中调整：
```yaml
logging:
  level:
    icu.debug.net.wg: DEBUG  # 详细日志
    # icu.debug.net.wg: INFO  # 正常日志
```

## 下一步

测试通过后，可以：
1. 添加更多复杂的网络拓扑测试
2. 测试并发场景
3. 压力测试
4. 集成测试
