package icu.debug.net.wg.service.module;

import icu.debug.net.wg.core.NetworkNodeConfigWrapper;
import icu.debug.net.wg.core.WireGuardConfigGenerator;
import icu.debug.net.wg.core.model.config.WireGuardNetProperties;
import icu.debug.net.wg.core.model.network.WireGuardNetworkStruct;
import icu.debug.net.wg.service.entity.GenerateRequest;
import icu.debug.net.wg.service.entity.GenerateResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-02-07 20:22
 */
@Component
public class WebConfigGenerateModule {


    public List<GenerateResult> generate(GenerateRequest request) {
        WireGuardNetworkStruct struct = request.getStruct();
        WireGuardNetProperties properties = request.getProperties();
        WireGuardConfigGenerator generator = new WireGuardConfigGenerator(struct, properties);
        List<NetworkNodeConfigWrapper> configs = generator.buildWireGuardIniConfigs();
        return configs.stream().map(GenerateResult::of).toList();
    }


}
