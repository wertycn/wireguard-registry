package icu.debug.net.wg.core.model.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import icu.debug.net.wg.core.model.config.WireGuardIniConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

import static icu.debug.net.wg.core.config.JacksonConfiguration.DEFAULT_JSON_MAPPER;

/**
 * WireGuard 网络定义
 */
@Getter
@Setter
@ToString
public class WireGuardNetworkStruct {

    private String name;

    private List<LocalAreaNetwork> localAreaNetworks;

    public static WireGuardNetworkStruct ofJson(String result) {
        try {
            return DEFAULT_JSON_MAPPER.readValue(result, WireGuardNetworkStruct.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("json parse error", e);
        }
    }

    public List<ServerNode> getServerNodes() {
        return getNetworkNodes().stream()
                .map(WireGuardNetworkNode::getServerNode)
                .filter(Objects::nonNull)
                .toList();

    }

    public List<WireGuardNetworkNode> getNetworkNodes() {
        Assert.notNull(localAreaNetworks, "localAreaNetworks it must not null");
        return localAreaNetworks.stream()
                .filter(localAreaNetwork -> localAreaNetwork.getNetworkNodes() != null)
                .flatMap(localAreaNetwork -> localAreaNetwork.getNetworkNodes().stream())
                .toList();
    }


}
