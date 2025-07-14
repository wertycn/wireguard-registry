# 集群模式简化说明

## 变更概述

根据您的反馈，我们将集群模式从复杂的节点间通信机制简化为**无状态节点 + 共享存储**的模式。

## 删除的组件

### 1. 复杂的集群管理类
- `ClusterManager` - 集群管理器
- `ClusterNode` - 集群节点
- `ClusterStatus` - 集群状态
- `ClusterNodeRegistry` - 集群节点注册表
- `ClusterHealthChecker` - 集群健康检查器
- `DatabaseClusterNodeRegistry` - 数据库集群节点注册表

### 2. 复杂的配置选项
- `cluster.discovery.type` - 集群发现类型
- `storage.type` - 存储类型配置
- 节点发现机制
- 节点健康检查机制

## 新增的组件

### 1. 简化的配置类
- `DeploymentMode` - 部署模式枚举
- `DeploymentConfig` - 部署配置类
- `ClusterConfiguration` - 集群模式配置类

### 2. 简化的配置文件
```yaml
wireguard:
  registry:
    mode: standalone  # 或 cluster
    node-id: ""       # 可选，空则自动生成
```

## 核心设计原理

### 1. 单机模式 (Standalone)
- 使用内存存储 (`MemoryConfigStorage`, `MemoryAuthStorage`)
- 或使用本地数据库 (SQLite)
- 适合开发环境和小规模部署

### 2. 集群模式 (Cluster)
- 强制使用分布式存储 (`DatabaseConfigStorage`, `DatabaseAuthStorage`)
- 支持 MySQL 和 MongoDB
- 所有节点完全无状态
- 通过共享数据库实现数据一致性

### 3. 模式切换
- 通过 `wireguard.registry.mode` 配置切换
- 通过 Spring Profile 选择存储类型
- 自动验证配置匹配性

## 架构优势

### 1. 简单性
- 无复杂的集群协议
- 无节点间通信
- 无状态同步机制

### 2. 可靠性
- 依赖成熟的数据库技术
- 数据库 ACID 特性保证一致性
- 标准的数据库运维经验

### 3. 可扩展性
- 水平扩展只需添加节点
- 节点可以随时加入或离开
- 负载均衡器处理流量分发

### 4. 运维友好
- 标准的应用部署模式
- 熟悉的数据库运维
- 简单的监控和故障处理

## 部署对比

### 之前的复杂设计
```
[节点1] ←→ [节点2] ←→ [节点3]
   ↓         ↓         ↓
[集群状态管理] [节点发现] [健康检查]
```

### 现在的简化设计
```
[节点1] → [共享数据库] ← [节点2]
                ↑
            [节点3]
```

## 配置示例

### 单机模式
```yaml
spring:
  profiles:
    active: memory  # 或 sqlite

wireguard:
  registry:
    mode: standalone
```

### 集群模式
```yaml
spring:
  profiles:
    active: mysql  # 或 mongodb

wireguard:
  registry:
    mode: cluster
    node-id: "node-001"
```

## 数据一致性保证

### 单机模式
- 单节点，天然一致性

### 集群模式
- 数据库事务保证原子性
- 数据库约束保证一致性
- 数据库隔离级别保证隔离性
- 数据库持久化保证持久性

## 与传统集群的差异

| 特性 | 传统集群 | 简化集群 |
|------|----------|----------|
| 节点通信 | 需要 | 不需要 |
| 状态同步 | 复杂的同步协议 | 数据库保证 |
| 故障检测 | 节点间检测 | 负载均衡器 |
| 脑裂处理 | 复杂的仲裁机制 | 数据库单点 |
| 扩展性 | 受限于协议 | 无限制 |

## 总结

这次简化将集群的复杂性从应用层下沉到数据库层，实现了：

1. **更简单的代码** - 删除了大量复杂的集群管理代码
2. **更简单的配置** - 只需配置模式和数据库连接
3. **更简单的部署** - 标准的无状态应用部署
4. **更简单的运维** - 依赖成熟的数据库技术栈

这种设计在实际生产环境中是非常成熟和可靠的解决方案，许多大型系统都采用类似的架构。