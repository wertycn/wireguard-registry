# WireGuard 注册中心认证架构

## 概述

本系统实现了双重认证机制，分别用于节点认证和管理控制台认证：

### 1. 节点认证（Node Authentication）
- **基于公钥密码学**：使用 Curve25519 算法（WireGuard 同款）
- **自动生成密钥**：每个节点拥有独立的密钥对
- **签名验证**：所有节点请求必须签名验证
- **防重放攻击**：基于时间戳的签名机制

### 2. 管理控制台认证（Admin Authentication）
- **基于JWT**：传统的用户名/密码认证
- **角色权限**：细粒度的权限控制
- **会话管理**：Token 撤销和过期管理

## 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                    WireGuard 认证架构                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐              ┌─────────────────┐           │
│  │   节点认证       │              │  管理控制台认证   │           │
│  │ (Node Auth)     │              │  (Admin Auth)   │           │
│  ├─────────────────┤              ├─────────────────┤           │
│  │ • Curve25519    │              │ • JWT Token     │           │
│  │ • 公钥/私钥      │              │ • 用户名/密码    │           │
│  │ • 签名验证       │              │ • 角色权限       │           │
│  │ • 时间戳防重放   │              │ • 会话管理       │           │
│  └─────────────────┘              └─────────────────┘           │
│           │                                 │                   │
│           ▼                                 ▼                   │
│  ┌─────────────────┐              ┌─────────────────┐           │
│  │ Node REST API   │              │ Admin REST API  │           │
│  │ /v1/registry/** │              │ /v1/admin/**    │           │
│  └─────────────────┘              └─────────────────┘           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 节点认证机制

### 1. 初始化流程

#### 1.1 管理员生成临时密钥
```http
POST /v1/admin/temp-keys?networkId=my-network
Authorization: Bearer <admin-jwt-token>

# 响应
{
  "code": 200,
  "data": {
    "keyId": "temp-key-12345",
    "networkId": "my-network",
    "privateKey": "base64-encoded-private-key",
    "publicKey": "base64-encoded-public-key",
    "expiresAt": "2024-01-01T10:05:00Z"
  }
}
```

#### 1.2 节点使用临时密钥注册
```http
POST /v1/registry/networks/my-network/nodes
X-Node-Auth: temp-key-12345
X-Signature: base64-encoded-signature
X-Timestamp: 1704096000

{
  "node": {
    "serverNode": {
      "name": "node-1",
      "endpoint": "192.168.1.100:51820"
    },
    "publicKey": "node-public-key"
  }
}
```

### 2. 签名生成

#### 客户端签名过程
```java
// 1. 准备数据
String data = "request-body-json";
long timestamp = System.currentTimeMillis() / 1000;
String message = data + ":" + timestamp;

// 2. 使用私钥签名
NodeSignatureHelper helper = new NodeSignatureHelper();
String signature = helper.signMessage(privateKey, message);

// 3. 设置请求头
httpRequest.setHeader("X-Node-Id", "node-1");
httpRequest.setHeader("X-Signature", signature);
httpRequest.setHeader("X-Timestamp", String.valueOf(timestamp));
```

#### 服务端验证过程
```java
// 1. 提取请求信息
String nodeId = request.getHeader("X-Node-Id");
String signature = request.getHeader("X-Signature");
long timestamp = Long.parseLong(request.getHeader("X-Timestamp"));
String data = requestBody;

// 2. 验证签名
NodeAuthService nodeAuth = new NodeAuthService();
boolean valid = nodeAuth.verifySignatureWithTimestamp(nodeId, signature, data, timestamp);

if (!valid) {
    throw new UnauthorizedException("Invalid signature");
}
```

### 3. 防重放攻击

- **时间戳验证**：签名必须包含时间戳，服务端验证时间戳在有效范围内（60秒）
- **随机数**：可选的随机数机制，防止相同请求重放

## 管理控制台认证机制

### 1. 用户角色系统

#### 角色定义
```java
public enum AdminRole {
    SUPER_ADMIN,    // 超级管理员 - 所有权限
    NETWORK_ADMIN,  // 网络管理员 - 网络和节点管理
    NODE_ADMIN,     // 节点管理员 - 节点管理
    MONITOR,        // 监控员 - 只读监控
    READ_ONLY       // 只读用户 - 查看配置
}
```

#### 权限矩阵
| 角色 | 网络管理 | 节点管理 | 用户管理 | 监控查看 | 配置查看 |
|------|----------|----------|----------|----------|----------|
| SUPER_ADMIN | ✅ | ✅ | ✅ | ✅ | ✅ |
| NETWORK_ADMIN | ✅ | ✅ | ❌ | ✅ | ✅ |
| NODE_ADMIN | ❌ | ✅ | ❌ | ✅ | ✅ |
| MONITOR | ❌ | ❌ | ❌ | ✅ | ✅ |
| READ_ONLY | ❌ | ❌ | ❌ | ❌ | ✅ |

### 2. 登录流程

#### 2.1 用户登录
```http
POST /v1/admin/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

# 响应
{
  "code": 200,
  "data": {
    "success": true,
    "message": "登录成功",
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
      "username": "admin",
      "email": "admin@wireguard.local",
      "roles": ["SUPER_ADMIN"],
      "active": true
    }
  }
}
```

#### 2.2 使用Token访问API
```http
GET /v1/admin/profile
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

# 响应
{
  "code": 200,
  "data": {
    "username": "admin",
    "email": "admin@wireguard.local",
    "roles": ["SUPER_ADMIN"],
    "active": true,
    "lastLoginAt": "2024-01-01T10:00:00Z"
  }
}
```

### 3. 用户管理

#### 3.1 创建用户
```http
POST /v1/admin/users
Authorization: Bearer <super-admin-token>
Content-Type: application/json

{
  "username": "netadmin",
  "password": "password123",
  "email": "netadmin@company.com",
  "roles": ["NETWORK_ADMIN"]
}
```

#### 3.2 修改密码
```http
POST /v1/admin/password
Authorization: Bearer <user-token>
Content-Type: application/json

{
  "oldPassword": "old123",
  "newPassword": "new456"
}
```

## 安全特性

### 1. 节点认证安全
- **强加密**：Curve25519 椭圆曲线加密
- **密钥轮换**：支持节点密钥更新
- **临时密钥**：初始注册使用短期临时密钥
- **签名验证**：所有请求必须签名验证
- **防重放**：时间戳验证防止重放攻击

### 2. 管理控制台安全
- **密码加密**：BCrypt 哈希存储
- **Token 过期**：JWT Token 有效期限制
- **Token 撤销**：支持主动撤销 Token
- **权限控制**：细粒度的角色权限管理
- **会话管理**：登录状态跟踪

## 使用示例

### 1. 节点客户端示例

```java
public class SecureRegistryClient {
    private final NodeSignatureHelper signHelper;
    private final String privateKey;
    private final String nodeId;
    
    public void registerNode(String networkId, WireGuardNetworkNode node) {
        // 1. 准备请求数据
        String requestBody = objectMapper.writeValueAsString(node);
        long timestamp = System.currentTimeMillis() / 1000;
        
        // 2. 生成签名
        String signature = signHelper.signWithTimestamp(privateKey, requestBody, timestamp);
        
        // 3. 发送请求
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(serverUrl + "/v1/registry/networks/" + networkId + "/nodes"))
            .header("Content-Type", "application/json")
            .header("X-Node-Id", nodeId)
            .header("X-Signature", signature)
            .header("X-Timestamp", String.valueOf(timestamp))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
            
        // 4. 处理响应
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // ...
    }
}
```

### 2. 管理控制台示例

```java
public class AdminClient {
    private String jwtToken;
    
    public void login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/v1/admin/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        
        LoginResult result = objectMapper.readValue(response.body(), LoginResult.class);
        if (result.isSuccess()) {
            this.jwtToken = result.getToken();
        }
    }
    
    public void createUser(String username, String password, String email, Set<AdminRole> roles) {
        CreateUserRequest request = new CreateUserRequest(username, password, email, roles);
        
        HttpResponse<String> response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/v1/admin/users"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        // ...
    }
}
```

## 配置说明

### 应用配置
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

### 安全建议

1. **JWT 密钥**：使用强随机密钥，定期轮换
2. **默认密码**：首次部署后立即修改默认管理员密码
3. **HTTPS**：生产环境必须使用 HTTPS
4. **防火墙**：限制管理接口的访问来源
5. **日志监控**：记录所有认证失败尝试
6. **密钥管理**：安全存储节点私钥，避免泄露

## 故障排除

### 常见问题

1. **节点认证失败**
   - 检查时间戳是否在有效范围内
   - 验证签名算法和密钥格式
   - 确认节点公钥已正确注册

2. **管理员登录失败**
   - 检查用户名和密码
   - 确认用户状态是否激活
   - 验证 JWT 配置

3. **权限不足**
   - 检查用户角色配置
   - 确认 API 端点的权限要求
   - 验证 Token 是否有效

### 调试日志
```yaml
logging:
  level:
    icu.debug.net.wg.core.auth: DEBUG
    icu.debug.net.wg.service.controller: DEBUG
```

通过这个双重认证架构，系统既保证了节点间的安全通信，又提供了完善的管理控制台功能，满足了不同场景的安全需求。