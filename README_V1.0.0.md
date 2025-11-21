# WireGuard Registry v1.0.0 - 配置存储和管理功能

## 📦 功能概述

本版本（v1.0.0 第一阶段）实现了 WireGuard 配置的持久化存储和 RESTful API 管理功能。

### 核心功能

1. **模块化存储层**
   - 完全独立的存储接口，不绑定任何 ORM 框架
   - 内存存储实现（开发/测试）
   - SQLite 存储实现（本地持久化）
   - 存储模块可按需引入

2. **配置管理 API**
   - 网络配置 CRUD 操作
   - 自动配置生成
   - 配置版本管理
   - 配置文件下载

3. **AMIS 前端界面**
   - 配置生成页面（原有功能增强）
   - 网络管理页面（新增）
   - 单一 index.html + app 参数路由

## 📁 项目结构

```
wireguard-registry/
├── wireguard-registry-core/          # 核心模块
│   └── storage/                       # 存储接口定义
│       ├── ConfigStorage.java
│       └── model/
│           ├── StoredNetwork.java
│           └── GeneratedConfig.java
│
├── wireguard-registry-storage/        # 存储实现模块（父模块）
│   ├── memory/                        # 内存存储
│   │   ├── MemoryConfigStorage.java
│   │   └── MemoryStorageConfiguration.java
│   └── sqlite/                        # SQLite 存储
│       ├── SqliteConfigStorage.java
│       ├── SqliteStorageConfiguration.java
│       ├── entity/                    # JPA Entity
│       └── repository/                # JPA Repository
│
├── wireguard-registry-service/        # Web 服务
│   ├── controller/
│   │   └── NetworkController.java    # REST API
│   ├── service/
│   │   └── NetworkConfigService.java # 业务服务
│   ├── entity/                        # 请求/响应对象
│   └── resources/
│       ├── application.yml            # 配置文件
│       └── static/
│           ├── index.html             # 前端入口（已更新）
│           └── options/
│               ├── form.json          # 配置生成页面
│               └── network-list.json  # 网络管理页面（新增）
│
├── API_TEST_GUIDE.md                  # API 测试指南
└── test-api.sh                        # 自动化测试脚本
```

## 🚀 快速开始

### 1. 编译项目

```bash
cd /home/user/wireguard-registry
mvn clean package -DskipTests
```

### 2. 启动服务

#### 使用内存存储（默认）
```bash
cd wireguard-registry-service/target
java -jar wireguard-registry-service-0.0.1-SNAPSHOT.jar
```

#### 使用 SQLite 存储
```bash
java -jar wireguard-registry-service-0.0.1-SNAPSHOT.jar \
  --wireguard.storage.type=sqlite \
  --wireguard.storage.sqlite.url=jdbc:sqlite:./data/wireguard.db
```

或使用环境变量：
```bash
export STORAGE_TYPE=sqlite
export SQLITE_PATH=./data/wireguard.db
java -jar wireguard-registry-service-0.0.1-SNAPSHOT.jar
```

### 3. 访问应用

- **配置生成页面**: http://localhost:8080?app=generate
- **网络管理页面**: http://localhost:8080?app=networks
- **API 文档**: 见 `API_TEST_GUIDE.md`

## 🔧 配置说明

### application.yml

```yaml
# 存储配置
wireguard:
  storage:
    type: memory  # 可选: memory, sqlite

    # SQLite 配置
    sqlite:
      url: jdbc:sqlite:./data/wireguard.db
```

### 环境变量

- `STORAGE_TYPE`: 存储类型 (memory/sqlite)
- `SQLITE_PATH`: SQLite 数据库文件路径

## 📡 API 接口

### 网络管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/networks | 创建网络 |
| GET | /api/v1/networks | 获取网络列表 |
| GET | /api/v1/networks/{id} | 获取网络详情 |
| PUT | /api/v1/networks/{id} | 更新网络 |
| DELETE | /api/v1/networks/{id} | 删除网络 |

### 配置管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/networks/{id}/configs | 获取网络所有配置 |
| GET | /api/v1/networks/{id}/configs/{nodeId} | 获取节点配置 |
| GET | /api/v1/networks/{id}/configs/{nodeId}/download | 下载配置文件 |
| POST | /api/v1/networks/{id}/regenerate | 重新生成配置 |
| GET | /api/v1/networks/{id}/version | 获取配置版本 |

### 响应格式（AMIS 格式）

```json
{
  "status": 0,      // 0=成功，-1=失败
  "msg": "success",
  "data": { ... }
}
```

## 🧪 测试

### 自动化测试

```bash
# 启动服务后运行
cd /home/user/wireguard-registry
./test-api.sh
```

测试脚本会自动执行以下测试：
1. 检查服务状态
2. 创建网络
3. 获取网络列表
4. 获取网络详情
5. 获取配置列表
6. 获取单个节点配置
7. 下载配置文件
8. 重新生成配置
9. 删除网络
10. 验证删除

### 手动测试

详细的测试步骤和 curl 命令请查看 `API_TEST_GUIDE.md`

## 📊 数据库 Schema

### SQLite 表结构

#### wg_networks
```sql
CREATE TABLE wg_networks (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    struct_json TEXT NOT NULL,
    properties_json TEXT,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

#### wg_configs
```sql
CREATE TABLE wg_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    network_id TEXT NOT NULL,
    node_id TEXT NOT NULL,
    node_name TEXT,
    config_json TEXT NOT NULL,
    config_text TEXT NOT NULL,
    version INTEGER NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    UNIQUE(network_id, node_id)
);
```

## 🔍 特性说明

### 1. 存储层独立性

- `ConfigStorage` 接口完全独立，不依赖任何 ORM
- 每个存储实现是独立的 Maven 模块
- 可以使用适合自己的 ORM（JPA、MyBatis、MongoDB 等）
- 按需引入存储模块，减少依赖

### 2. 自动配置

基于 Spring Boot Auto-Configuration 机制：
- 根据 `wireguard.storage.type` 自动选择存储实现
- 无需手动配置 Bean
- 支持配置文件和环境变量

### 3. AMIS 前端集成

- 单一 `index.html` 通过 `?app=` 参数控制
- 侧边导航菜单切换功能
- AMIS CRUD 组件展示网络列表
- 配置查看和下载功能

### 4. 配置版本管理

- 每个网络维护一个版本号
- 配置变更时版本号自动递增
- 支持查询当前版本
- 为后续的配置订阅功能做准备

## 🎯 使用场景

### 场景 1: 临时测试

1. 使用内存存储（默认）
2. 在配置生成页面创建配置
3. 查看生成结果
4. 重启后数据消失

### 场景 2: 持久化存储

1. 使用 SQLite 存储
2. 创建网络并保存
3. 重启服务后数据仍然存在
4. 可以继续管理和修改

### 场景 3: 配置管理

1. 在网络管理页面查看所有网络
2. 查看每个网络的配置详情
3. 下载节点配置文件
4. 重新生成配置（版本号递增）
5. 删除不需要的网络

## 🔜 后续计划

### 第二阶段：扩展存储支持
- MySQL 存储模块
- PostgreSQL 存储模块
- MongoDB 存储模块

### 第三阶段：注册中心功能
- 节点动态注册
- 心跳监控
- 配置自动下发
- 客户端 SDK

### 第四阶段：认证授权
- 节点认证（Curve25519）
- 管理员认证（JWT）
- 角色权限管理
- API 访问控制

### 第五阶段：高级功能
- 配置订阅推送
- 集群部署支持
- 监控和日志
- Web 管理控制台优化

## 📝 提交信息

- **分支**: `claude/config-registry-v1-01Pnb8BD1qYwMXAX9cqAQRWV`
- **基于**: commit `aa154d7` (Update README.md)
- **提交**: `aa29eba` (feat: 实现配置存储和管理功能)

## 🐛 已知问题

### 当前编译问题

由于环境网络 DNS 解析问题，Maven 无法下载依赖。解决方法：

1. **修复网络后重新编译**
   ```bash
   mvn clean package -DskipTests
   ```

2. **或使用已有环境的 jar 包**
   如果在其他环境已编译，可直接使用 jar 文件

3. **配置代理**
   ```xml
   <!-- settings.xml -->
   <proxies>
     <proxy>
       <host>proxy.example.com</host>
       <port>8080</port>
     </proxy>
   </proxies>
   ```

## 📚 相关文档

- `API_TEST_GUIDE.md` - API 测试完整指南
- `test-api.sh` - 自动化测试脚本
- `application.yml` - 应用配置文件
- `README.md` - 项目主文档（原有）

## 💡 贡献

本功能基于原有的 `WireGuardConfigGenerator` 实现，在保持兼容性的同时添加了：
- 持久化存储能力
- RESTful API 接口
- 配置管理界面
- 模块化架构设计

## 📄 许可证

与主项目保持一致

---

**当前版本**: v1.0.0 (第一阶段)
**最后更新**: 2025-11-21
**代码状态**: ✅ 已提交并推送到远程分支
