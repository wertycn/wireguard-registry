package icu.debug.net.wg.storage.memory;

import icu.debug.net.wg.core.storage.ConfigStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 内存存储配置类
 *
 * <p>当配置项 wireguard.storage.type=memory 时自动启用
 *
 * @author WireGuard Registry Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "wireguard.storage.type", havingValue = "memory", matchIfMissing = true)
public class MemoryStorageConfiguration {

    @Bean
    public ConfigStorage configStorage() {
        log.info("Initializing Memory ConfigStorage");
        return new MemoryConfigStorage();
    }
}
