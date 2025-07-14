package icu.debug.net.wg.core.config;

/**
 * 部署模式配置
 * 
 * @author Administrator
 * @since 2024-01-01
 */
public enum DeploymentMode {
    
    /**
     * 单机模式
     * - 使用内存存储或本地数据库（SQLite）
     * - 适合开发环境和小规模部署
     */
    STANDALONE,
    
    /**
     * 集群模式
     * - 强制使用分布式存储（MySQL、MongoDB）
     * - 所有节点无状态，通过共享存储实现数据一致性
     * - 适合生产环境和大规模部署
     */
    CLUSTER
    
}