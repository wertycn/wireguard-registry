package icu.debug.net.wg.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import icu.debug.net.wg.core.WireGuardConfigGenerator;
import icu.debug.net.wg.core.auth.AdminAuthService;
import icu.debug.net.wg.core.auth.NodeAuthService;
import icu.debug.net.wg.core.registry.ConfigRegistry;
import icu.debug.net.wg.core.registry.impl.DefaultConfigRegistry;
import icu.debug.net.wg.core.storage.ConfigStorage;
import icu.debug.net.wg.core.storage.impl.MemoryConfigStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 注册中心配置类
 */
@Configuration
public class RegistryConfiguration {

    /**
     * 配置Jackson ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * 默认内存存储配置
     */
    @Bean
    @ConditionalOnProperty(name = "wireguard.registry.storage.type", havingValue = "memory", matchIfMissing = true)
    public ConfigStorage memoryConfigStorage() {
        return new MemoryConfigStorage();
    }

    /**
     * 配置生成器
     */
    @Bean
    public WireGuardConfigGenerator wireGuardConfigGenerator() {
        return new WireGuardConfigGenerator();
    }

    /**
     * 默认配置注册中心
     */
    @Bean
    public ConfigRegistry configRegistry(ConfigStorage configStorage, WireGuardConfigGenerator configGenerator) {
        return new DefaultConfigRegistry(configStorage, configGenerator);
    }

    /**
     * 节点认证服务
     */
    @Bean
    public NodeAuthService nodeAuthService() {
        return new NodeAuthService();
    }

    /**
     * 管理员认证服务
     */
    @Bean
    public AdminAuthService adminAuthService(@Value("${wireguard.registry.auth.jwt-secret}") String jwtSecret) {
        return new AdminAuthService(jwtSecret);
    }
}