package icu.debug.net.wg.service.entity;

import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import lombok.Data;

@Data
public class CreateNetworkRequest {
    private String id;
    private String name;
    private WireGuardNetworkStruct struct;
    private WireGuardNetProperties properties;
}
