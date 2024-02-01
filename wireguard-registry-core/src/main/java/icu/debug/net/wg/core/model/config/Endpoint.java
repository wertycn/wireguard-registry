package icu.debug.net.wg.core.model.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-01-29 0:40
 */
@Getter
@AllArgsConstructor
public class Endpoint {

    /**
     * 地址 i
     */
    private String address;

    /**
     * 端口
     */
    private Integer port;

    @Override
    public String toString() {
        if (StringUtils.hasLength(getAddress()) && getPort() != null) {
            return getAddress() + ":" + getPort();
        }
        return "";
    }

}
