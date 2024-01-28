package icu.debug.net.wg.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-01-28 23:12
 */
public class JacksonConfiguration {

    public static final ObjectMapper DEFAULT_JSON_MAPPER = getDefaultJsonMapper();

    private static ObjectMapper getDefaultJsonMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return objectMapper;
    }
}
