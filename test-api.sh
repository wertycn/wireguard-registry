#!/bin/bash

# WireGuard Registry API 自动化测试脚本

set -e

BASE_URL="http://localhost:8080"
NETWORK_ID="test-network-$(date +%s)"

echo "========================================="
echo "WireGuard Registry API 测试"
echo "========================================="
echo ""

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

function test_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

function test_fail() {
    echo -e "${RED}✗ $1${NC}"
    exit 1
}

function test_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# 检查服务是否启动
echo "1. 检查服务状态..."
if curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    test_success "服务正常运行"
else
    test_fail "服务未启动，请先启动服务"
fi

# 测试创建网络
echo ""
echo "2. 创建网络: ${NETWORK_ID}..."
CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/networks" \
  -H "Content-Type: application/json" \
  -d "{
    \"id\": \"${NETWORK_ID}\",
    \"name\": \"测试网络\",
    \"struct\": {
      \"address\": \"10.0.0.1\",
      \"netmask\": \"255.255.255.0\",
      \"localAreaNetworks\": [
        {
          \"name\": \"lan1\",
          \"networkType\": \"POINT_TO_SITE\",
          \"networkNodes\": [
            {
              \"serverNode\": {
                \"hostname\": \"server1\",
                \"endpoint\": {
                  \"host\": \"192.168.1.1\",
                  \"port\": 51820
                },
                \"endpointType\": \"PUBLIC\"
              }
            },
            {
              \"serverNode\": {
                \"hostname\": \"client1\",
                \"endpointType\": \"PRIVATE\"
              }
            }
          ]
        }
      ]
    },
    \"properties\": {
      \"address\": \"10.0.0.1\",
      \"netmask\": \"255.255.255.0\",
      \"listen_port\": 51820
    }
  }")

if echo "$CREATE_RESPONSE" | grep -q '"status":0'; then
    test_success "网络创建成功"
    test_info "响应: $CREATE_RESPONSE"
else
    test_fail "网络创建失败: $CREATE_RESPONSE"
fi

# 测试获取网络列表
echo ""
echo "3. 获取网络列表..."
LIST_RESPONSE=$(curl -s "${BASE_URL}/api/v1/networks")

if echo "$LIST_RESPONSE" | grep -q "${NETWORK_ID}"; then
    test_success "网络列表包含新创建的网络"
    TOTAL=$(echo "$LIST_RESPONSE" | grep -o '"total":[0-9]*' | cut -d: -f2)
    test_info "当前网络总数: $TOTAL"
else
    test_fail "网络列表中未找到新创建的网络"
fi

# 测试获取网络详情
echo ""
echo "4. 获取网络详情..."
DETAIL_RESPONSE=$(curl -s "${BASE_URL}/api/v1/networks/${NETWORK_ID}")

if echo "$DETAIL_RESPONSE" | grep -q '"status":0'; then
    test_success "网络详情获取成功"
    VERSION=$(echo "$DETAIL_RESPONSE" | grep -o '"version":[0-9]*' | cut -d: -f2)
    test_info "当前版本: $VERSION"
else
    test_fail "网络详情获取失败"
fi

# 测试获取配置列表
echo ""
echo "5. 获取网络配置列表..."
CONFIGS_RESPONSE=$(curl -s "${BASE_URL}/api/v1/networks/${NETWORK_ID}/configs")

if echo "$CONFIGS_RESPONSE" | grep -q '"status":0'; then
    test_success "配置列表获取成功"
    if echo "$CONFIGS_RESPONSE" | grep -q "server1" && echo "$CONFIGS_RESPONSE" | grep -q "client1"; then
        test_success "配置包含 server1 和 client1 节点"
    else
        test_fail "配置节点不完整"
    fi
else
    test_fail "配置列表获取失败"
fi

# 测试获取单个节点配置
echo ""
echo "6. 获取 server1 节点配置..."
NODE_CONFIG=$(curl -s "${BASE_URL}/api/v1/networks/${NETWORK_ID}/configs/server1")

if echo "$NODE_CONFIG" | grep -q '"status":0'; then
    test_success "节点配置获取成功"
    if echo "$NODE_CONFIG" | grep -q '"configText"'; then
        test_success "配置包含 configText 字段"
    fi
else
    test_fail "节点配置获取失败"
fi

# 测试下载配置文件
echo ""
echo "7. 下载配置文件..."
curl -s "${BASE_URL}/api/v1/networks/${NETWORK_ID}/configs/server1/download" -o "/tmp/server1.conf"

if [ -f "/tmp/server1.conf" ] && [ -s "/tmp/server1.conf" ]; then
    test_success "配置文件下载成功"
    test_info "文件大小: $(wc -c < /tmp/server1.conf) bytes"

    if grep -q "\[Interface\]" "/tmp/server1.conf"; then
        test_success "配置文件格式正确"
    fi
else
    test_fail "配置文件下载失败"
fi

# 测试重新生成配置
echo ""
echo "8. 重新生成配置..."
REGEN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/networks/${NETWORK_ID}/regenerate")

if echo "$REGEN_RESPONSE" | grep -q '"status":0'; then
    test_success "配置重新生成成功"
    NEW_VERSION=$(echo "$REGEN_RESPONSE" | grep -o '"version":[0-9]*' | cut -d: -f2)
    test_info "新版本号: $NEW_VERSION"
else
    test_fail "配置重新生成失败"
fi

# 测试删除网络
echo ""
echo "9. 删除网络..."
DELETE_RESPONSE=$(curl -s -X DELETE "${BASE_URL}/api/v1/networks/${NETWORK_ID}")

if echo "$DELETE_RESPONSE" | grep -q '"status":0'; then
    test_success "网络删除成功"
else
    test_fail "网络删除失败"
fi

# 验证删除
echo ""
echo "10. 验证网络已删除..."
VERIFY_RESPONSE=$(curl -s "${BASE_URL}/api/v1/networks/${NETWORK_ID}")

if echo "$VERIFY_RESPONSE" | grep -q '"status":-1' || echo "$VERIFY_RESPONSE" | grep -q 'not found'; then
    test_success "网络已成功删除"
else
    test_fail "网络删除验证失败"
fi

echo ""
echo "========================================="
echo -e "${GREEN}所有测试通过！${NC}"
echo "========================================="

# 清理
rm -f /tmp/server1.conf
