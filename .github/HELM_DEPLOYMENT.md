# Helm 自动化部署配置指南

## 需要的 GitHub Secrets 配置

在 GitHub Repository Settings → Secrets and variables → Actions 中配置以下 secrets：

### 1. Kubernetes 配置
```bash
KUBE_CONFIG: <base64编码的kubeconfig文件内容>
```

获取方法：
```bash
# 将 kubeconfig 文件内容进行 base64 编码
cat ~/.kube/config | base64 -w 0
```

### 2. 企业微信通知（可选，继承现有配置）
```bash
WECHAT_CORP_ID: <企业微信企业ID>
WECHAT_CROP_SECRET: <企业微信应用密钥>
WECHAT_AGENT_ID: <企业微信应用ID>
```

### 3. Docker 仓库认证（可选，用于构建镜像）
```bash
DOCKER_HUB_REGISTRY_HW_HK: <私有Docker仓库地址>
DOCKER_HUB_USERNAME_HW: <Docker仓库用户名>
DOCKER_HUB_PASSWORD_HW: <Docker仓库密码>
PUB_DOCKER_HUB_USERNAME: <公共Docker仓库用户名>
PUB_DOCKER_HUB_TOKEN: <公共Docker仓库访问令牌>
```

## 部署流程

### 自动触发
- 推送到 `main` 或 `release-*` 分支时自动部署到生产环境
- 使用 commit SHA 作为镜像标签

### 手动触发
1. 进入 GitHub Actions → "Helm Deploy to Kubernetes"
2. 点击 "Run workflow"
3. 选择环境（prod/staging）
4. 可选：指定镜像标签（默认为 latest）

## 环境说明

- **生产环境 (prod)**: main 和 release-* 分支
- **预发布环境 (staging)**: 其他分支

## 验证部署

部署完成后会自动验证：
1. 检查所有 Kubernetes 资源状态
2. 检查 Helm release 状态
3. 发送企业微信通知

## 回滚机制

如果部署失败，会自动尝试回滚到上一个版本。

## 自定义配置

可以通过修改 `app-chart/values.yaml` 文件来自定义部署配置，或使用 `--set` 参数覆盖特定值。