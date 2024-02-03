package icu.debug.net.wg.client.config;

import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-04 1:55
 */
@Configuration
public class DefaultConfiguration {

    @Bean()
    @ConfigurationProperties(prefix = "wireguard.default")
    public WireGuardNetProperties getDefaultProperties() {
        return new WireGuardNetProperties();
    }

}
