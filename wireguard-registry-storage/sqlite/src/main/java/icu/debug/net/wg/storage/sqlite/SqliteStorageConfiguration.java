package icu.debug.net.wg.storage.sqlite;

import com.fasterxml.jackson.databind.ObjectMapper;
import icu.debug.net.wg.core.storage.ConfigStorage;
import icu.debug.net.wg.storage.sqlite.repository.ConfigRepository;
import icu.debug.net.wg.storage.sqlite.repository.NetworkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "wireguard.storage.type", havingValue = "sqlite")
@EnableJpaRepositories(basePackages = "icu.debug.net.wg.storage.sqlite.repository")
@EntityScan(basePackages = "icu.debug.net.wg.storage.sqlite.entity")
public class SqliteStorageConfiguration {

    @Bean
    public ConfigStorage configStorage(NetworkRepository networkRepository,
                                      ConfigRepository configRepository,
                                      ObjectMapper objectMapper) {
        log.info("Initializing SQLite ConfigStorage");
        return new SqliteConfigStorage(networkRepository, configRepository, objectMapper);
    }
}
