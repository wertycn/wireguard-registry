package icu.debug.net.wg.service.entity;

import icu.debug.net.wg.core.NetworkNodeConfigWrapper;
import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import lombok.*;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-07 19:04
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GenerateResult {

    /**
     * 主机名
     */
    private String hostname;

    /**
     * 配置内容
     */
    private String config;


    public static GenerateResult of(NetworkNodeConfigWrapper config) {
        return new GenerateResult(config.getHostName(), config.getConfig().toIniString());
    }
}
