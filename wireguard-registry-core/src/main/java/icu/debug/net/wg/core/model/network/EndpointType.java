package icu.debug.net.wg.core.model.network;

import icu.debug.net.wg.core.model.config.Endpoint;

import java.util.Optional;

/**
 * @author hanjinxiang@debug.icu
 * @date 2024-01-29 0:46
 */
public enum EndpointType {

    /**
     * 公网地址
     */
    PUBLIC,

    /**
     * 内网地址
     */
    PRIVATE,

    /**
     * 未知或不可达
     */
    UNKNOWN;

    public static Optional<Endpoint> buildEndpoint(EndpointType type, ServerNode node, Integer port) {
        if (type == PUBLIC) {
            return Optional.of(new Endpoint(node.getPublicAddress(), port));
        }
        if (type == PRIVATE) {
            return Optional.of(new Endpoint(node.getPrivateAddress(), port));
        }
        return Optional.empty();
    }

}
