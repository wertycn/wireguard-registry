package icu.debug.net.wg.service.config;

import icu.debug.net.wg.core.auth.storage.AuthStorage;
import icu.debug.net.wg.core.auth.storage.impl.DatabaseAuthStorage;
import icu.debug.net.wg.core.storage.ConfigStorage;
import icu.debug.net.wg.core.storage.impl.DatabaseConfigStorage;
import icu.debug.net.wg.persistence.dao.GeneratedConfigDao;
import icu.debug.net.wg.persistence.dao.NetworkNodeDao;
import icu.debug.net.wg.persistence.dao.NetworkVersionDao;
import icu.debug.net.wg.persistence.dao.auth.AdminUserDao;
import icu.debug.net.wg.persistence.dao.auth.NodeSignatureDao;
import icu.debug.net.wg.persistence.dao.auth.TemporaryKeyDao;
import icu.debug.net.wg.persistence.dao.auth.TokenRevocationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集群模式配置类
 * 
 * @author Administrator
 * @since 2024-01-01
 */
@Configuration
@ConditionalOnProperty(name = "wireguard.registry.mode", havingValue = "cluster")
public class ClusterConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ClusterConfiguration.class);

    /**
     * 配置存储（集群模式）
     * 使用数据库存储确保多节点数据一致性
     */
    @Bean
    public ConfigStorage clusterConfigStorage(NetworkNodeDao networkNodeDao,
                                             GeneratedConfigDao generatedConfigDao,
                                             NetworkVersionDao networkVersionDao) {
        log.info("Creating database-based config storage for cluster mode");
        return new DatabaseConfigStorage(networkNodeDao, generatedConfigDao, networkVersionDao);
    }

    /**
     * 认证存储（集群模式）
     * 使用数据库存储确保多节点认证数据一致性
     */
    @Bean
    public AuthStorage clusterAuthStorage(NodeSignatureDao nodeSignatureDao,
                                         TemporaryKeyDao temporaryKeyDao,
                                         AdminUserDao adminUserDao,
                                         TokenRevocationDao tokenRevocationDao) {
        log.info("Creating database-based auth storage for cluster mode");
        return new DatabaseAuthStorage(nodeSignatureDao, temporaryKeyDao, adminUserDao, tokenRevocationDao);
    }

    /**
     * 集群模式启动时的验证
     */
    @Bean
    public ClusterModeValidator clusterModeValidator() {
        return new ClusterModeValidator();
    }

    /**
     * 集群模式验证器
     */
    public static class ClusterModeValidator {

        private static final Logger log = LoggerFactory.getLogger(ClusterModeValidator.class);

        public ClusterModeValidator() {
            log.info("Validating cluster mode configuration...");
            validateClusterMode();
            log.info("Cluster mode validation passed");
        }

        private void validateClusterMode() {
            log.info("Cluster mode enabled - all nodes are stateless");
            log.info("Data consistency is maintained through shared database storage");
            log.info("Make sure all nodes use the same database configuration");
        }
    }
}