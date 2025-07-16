package icu.debug.net.wg.service.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 创建网络请求实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNetworkRequest {
    
    private String networkId;
    private String name;
    private String description;
    private String address;
    private String netmask;
} 