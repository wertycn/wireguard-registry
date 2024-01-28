package icu.debug.net.wg.core.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * WireGuard 配置定义
 */
@Getter
@Setter
@ToString
public class WireGuardConfig {

    private final static String DEFAULT_NAME = "wg0";

    /**
     * 配置文件名
     */
    private String name;

    private WireGuardInterface wgInterface;

    private List<WireGuardPeer> peers;

    /**
     * 生成Ini 格式文本配置
     *
     * @return
     */
    public String toIniString() {
        StringBuilder iniBuilder = new StringBuilder();
        iniBuilder.append(wgInterface.toIniString()+"\n");
        peers.forEach(item -> iniBuilder.append("\n\n"+item.toIniString()));
        return iniBuilder.toString();
    }


}
