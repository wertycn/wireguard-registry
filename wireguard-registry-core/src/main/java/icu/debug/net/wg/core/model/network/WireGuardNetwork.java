package icu.debug.net.wg.core.model.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static icu.debug.net.wg.core.config.JacksonConfiguration.DEFAULT_JSON_MAPPER;

/**
 * WireGuard 网络定义
 */
@Getter
@Setter
@ToString
public class WireGuardNetwork {

    private String name;


    private List<LocalAreaNetwork> localAreaNetworks;

    public static WireGuardNetwork ofJson(String result) {
        try {
            return DEFAULT_JSON_MAPPER.readValue(result, WireGuardNetwork.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("json parse error", e);
        }
    }


}
