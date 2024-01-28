package icu.debug.net.wg.core.model.network;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 局域网
 */
@Getter
@Setter
@ToString
public class LocalAreaNetwork {

    private String name;

    private NetworkType networkType;

    /**
     * 局域网内的服务器列表
     */
    private List<WireGuardNetworkNode> networkNodes;




}
