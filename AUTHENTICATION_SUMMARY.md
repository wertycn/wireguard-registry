# WireGuard 注册中心认证系统实现总结

## 🎯 实现目标

基于您的要求，我们实现了一个双重认证机制：

### 1. 节点认证（Node Authentication）
- **基于WireGuard同款算法**：使用 Curve25519 椭圆曲线加密
- **每个节点独立认证**：每个节点拥有自己的公私钥对
- **安全且轻量**：参考 WireGuard 本身的认证机制
- **临时密钥初始化**：提供短期临时密钥用于客户端初始化

### 2. 管理控制台认证（Admin Authentication）
- **传统JWT认证**：管理员使用用户名/密码登录
- **角色权限控制**：细粒度的权限管理
- **完全分离**：与节点认证机制完全独立

## 🏗️ 架构实现

### 核心组件

#### 1. 节点认证核心类
- **`NodeAuthService`**: 节点认证服务，负责签名验证和密钥管理
- **`NodeSignatureHelper`**: 签名助手类，用于客户端生成签名
- **`TemporaryKey`**: 临时密钥数据类，用于节点初始注册

#### 2. 管理控制台认证核心类
- **`AdminAuthService`**: 管理员认证服务，负责JWT和用户管理
- **`AdminUser`**: 管理员用户数据类
- **`AdminRole`**: 管理员角色枚举，包含权限控制

#### 3. REST API 控制器
- **`AdminController`**: 管理控制台API（`/v1/admin/**`）
- **`RegistryController`**: 节点注册API（`/v1/registry/**`）

## 🔐 安全特性

### 节点认证安全
- ✅ **Curve25519 加密**：与 WireGuard 同级别的加密强度
- ✅ **签名验证**：所有节点请求必须使用私钥签名
- ✅ **防重放攻击**：基于时间戳的签名验证（60秒有效期）
- ✅ **临时密钥机制**：初始注册使用短期临时密钥（5分钟有效期）
- ✅ **密钥隔离**：每个节点独立的密钥对

### 管理控制台安全
- ✅ **JWT认证**：标准的Token认证机制
- ✅ **密码加密**：BCrypt哈希存储密码
- ✅ **Token撤销**：支持主动撤销Token
- ✅ **角色权限**：5种角色，细粒度权限控制
- ✅ **会话管理**：Token过期和用户状态管理

## 🚀 使用流程

### 节点注册流程

#### 1. 管理员生成临时密钥
```bash
curl -X POST "http://localhost:8080/v1/admin/temp-keys?networkId=my-network" \
  -H "Authorization: Bearer <admin-jwt-token>" \
  -H "Content-Type: application/json"
```

#### 2. 节点使用临时密钥注册
```bash
curl -X POST "http://localhost:8080/v1/registry/networks/my-network/nodes" \
  -H "X-Node-Auth: temp-key-12345" \
  -H "X-Signature: <signature>" \
  -H "X-Timestamp: <timestamp>" \
  -H "Content-Type: application/json" \
  -d '{
    "node": {
      "serverNode": {
        "name": "node-1",
        "endpoint": "192.168.1.100:51820"
      },
      "publicKey": "base64-encoded-public-key"
    }
  }'
```

#### 3. 后续请求使用节点密钥
```bash
curl -X GET "http://localhost:8080/v1/registry/networks/my-network/nodes/node-1/config" \
  -H "X-Node-Id: node-1" \
  -H "X-Signature: <signature>" \
  -H "X-Timestamp: <timestamp>"
```

### 管理控制台流程

#### 1. 管理员登录
```bash
curl -X POST "http://localhost:8080/v1/admin/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 2. 使用JWT访问管理API
```bash
curl -X GET "http://localhost:8080/v1/admin/profile" \
  -H "Authorization: Bearer <jwt-token>"
```

#### 3. 创建用户
```bash
curl -X POST "http://localhost:8080/v1/admin/users" \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "netadmin",
    "password": "password123",
    "email": "netadmin@company.com",
    "roles": ["NETWORK_ADMIN"]
  }'
```

## 📝 API 端点总结

### 节点认证 API (`/v1/registry/**`)
- `POST /v1/registry/networks/{networkId}/nodes` - 注册节点
- `GET /v1/registry/networks/{networkId}/nodes/{nodeId}/config` - 获取节点配置
- `POST /v1/registry/networks/{networkId}/nodes/{nodeId}/heartbeat` - 发送心跳
- `DELETE /v1/registry/networks/{networkId}/nodes/{nodeId}` - 注销节点

### 管理控制台 API (`/v1/admin/**`)
- `POST /v1/admin/login` - 管理员登录
- `POST /v1/admin/logout` - 管理员登出
- `GET /v1/admin/profile` - 获取当前用户信息
- `POST /v1/admin/password` - 修改密码
- `POST /v1/admin/users` - 创建用户
- `GET /v1/admin/users` - 获取所有用户
- `POST /v1/admin/users/{username}/status` - 启用/禁用用户
- `POST /v1/admin/temp-keys` - 生成临时密钥

## 🔧 配置说明

### 应用配置 (`application.yml`)
```yaml
wireguard:
  registry:
    auth:
      jwt-secret: "your-jwt-secret-key-here"
      temp-key-expiry: 300  # 临时密钥有效期（秒）
      signature-expiry: 60  # 签名有效期（秒）
    admin:
      default-username: admin
      default-password: admin123
```

### 角色权限配置
```java
// 5种预定义角色
SUPER_ADMIN     // 超级管理员 - 所有权限
NETWORK_ADMIN   // 网络管理员 - 网络和节点管理
NODE_ADMIN      // 节点管理员 - 节点管理
MONITOR         // 监控员 - 只读监控
READ_ONLY       // 只读用户 - 查看配置
```

## 📦 依赖项

新增的关键依赖：
```xml
<!-- JWT支持 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- Spring Security密码加密 -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>

<!-- 现有的Curve25519支持 -->
<dependency>
    <groupId>org.whispersystems</groupId>
    <artifactId>curve25519-java</artifactId>
    <version>0.5.0</version>
</dependency>
```

## 🛡️ 安全建议

### 生产环境配置
1. **修改默认密码**：首次部署后立即修改管理员密码
2. **强化JWT密钥**：使用强随机密钥，定期轮换
3. **启用HTTPS**：所有API访问必须使用HTTPS
4. **网络隔离**：管理接口与节点接口分离部署
5. **日志监控**：记录所有认证失败尝试

### 密钥管理
1. **节点密钥安全**：节点私钥安全存储，避免泄露
2. **临时密钥轮换**：定期清理过期的临时密钥
3. **密钥备份**：重要节点密钥的安全备份

## 🎉 实现优势

### 1. 完全符合您的要求
- ✅ **节点独立认证**：每个节点有自己的认证凭证
- ✅ **服务端识别**：基于认证凭证识别客户端
- ✅ **参考WireGuard机制**：使用同款Curve25519算法
- ✅ **轻量安全**：无需复杂的证书管理
- ✅ **临时密钥初始化**：短期临时密钥用于初始化

### 2. 管理控制台分离
- ✅ **独立认证机制**：管理控制台使用JWT认证
- ✅ **角色权限控制**：细粒度的权限管理
- ✅ **用户管理**：完整的用户生命周期管理

### 3. 安全性强化
- ✅ **防重放攻击**：时间戳验证机制
- ✅ **签名验证**：所有请求必须签名
- ✅ **权限隔离**：不同角色的权限严格分离
- ✅ **会话管理**：Token撤销和过期管理

## 📖 使用文档

详细的使用文档和示例请参考：
- **[AUTHENTICATION.md](./AUTHENTICATION.md)** - 完整的认证架构文档
- **[REGISTRY.md](./REGISTRY.md)** - 注册中心功能文档

## 🔥 立即开始

1. **启动服务**：
   ```bash
   java -jar wireguard-registry-service.jar
   ```

2. **管理员登录**：
   ```bash
   curl -X POST "http://localhost:8080/v1/admin/login" \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "admin123"}'
   ```

3. **生成临时密钥**：
   ```bash
   curl -X POST "http://localhost:8080/v1/admin/temp-keys?networkId=test" \
     -H "Authorization: Bearer <token>"
   ```

4. **节点注册**：
   使用临时密钥注册节点，之后就可以用节点自己的密钥进行后续操作。

---

**总结**：我们成功实现了一个完整的双重认证系统，既满足了节点之间的安全通信需求，又提供了完善的管理控制台功能。系统设计参考了WireGuard的认证机制，确保了安全性和轻量级特性。