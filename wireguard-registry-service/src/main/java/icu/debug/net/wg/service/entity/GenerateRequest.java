package icu.debug.net.wg.service.entity;

import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-07 19:04
 */
@Getter
@Setter
@ToString
public class GenerateRequest {

    /**
     * 全局配置
     */
    private WireGuardNetProperties properties;

    /**
     * 组网规划
     */
    private WireGuardNetworkStruct struct;

}
