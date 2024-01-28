package icu.debug.net.wg.core.model.network;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 服务器元数据
 */
@Getter
@Setter
@ToString
public class ServerNode {

    /**
     * 主机名 全局唯一
     */
    private String hostname;

    /**
     * 公网地址
     */
    private String publicAddress;

    /**
     * 内网地址
     */
    private String privateAddress;
}
