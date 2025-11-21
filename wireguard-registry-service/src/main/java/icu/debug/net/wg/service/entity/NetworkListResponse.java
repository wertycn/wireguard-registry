package icu.debug.net.wg.service.entity;

import icu.debug.net.wg.core.storage.model.StoredNetwork;
import lombok.Data;

import java.util.List;

/**
 * AMIS CRUD 列表响应格式
 */
@Data
public class NetworkListResponse {
    private int status = 0;
    private String msg = "success";
    private List<StoredNetwork> items;
    private int total;

    public static NetworkListResponse success(List<StoredNetwork> items) {
        NetworkListResponse response = new NetworkListResponse();
        response.items = items;
        response.total = items.size();
        return response;
    }
}
