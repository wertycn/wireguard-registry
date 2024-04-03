# WireGuard 跨云组网配置生成器

[![Java CI with Maven](https://github.com/wertycn/wireguard-registry/actions/workflows/maven.yml/badge.svg)](https://github.com/wertycn/wireguard-registry/actions/workflows/maven.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=wertycn_wireguard-registry&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=wertycn_wireguard-registry)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=wertycn_wireguard-registry&metric=coverage)](https://sonarcloud.io/summary/new_code?id=wertycn_wireguard-registry)

## 项目介绍
用于跨云，多局域网，中继的多种复杂模式组合的wireguard配置生成器工具，可以用于构建复杂的网络拓扑，同时最大化的减少公网流量(局域网内走内网通信)
应用场景：
1. K8s 跨公有云+家庭宽带组网

## 快速开始

容器部署:
```
docker run --name  -d -p 8080:8080 debugicu/wireguard-registry:latest
```

## 功能模块

目前已完成配置生成器部分，体验地址: [https://wireguard.debug.icu](https://wireguard.debug.icu)

[-] 配置模型  
[-] 配置文件生成  
[-] 静态网络模型  
[-] 静态网络模型转配置模型  
[-] 默认配置合并，属性补充 
[-] API server
    - 提供在线配置生成服务
    参数校验  

后续计划继续开发功能：    
[] 注册中心模式, 中心统一下发配置，节点变动时无需手动调整每个节点的配置
[] 流量数据监控采集  
