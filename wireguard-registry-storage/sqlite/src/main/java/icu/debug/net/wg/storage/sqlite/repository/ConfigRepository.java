package icu.debug.net.wg.storage.sqlite.repository;

import icu.debug.net.wg.storage.sqlite.entity.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, Long> {

    Optional<ConfigEntity> findByNetworkIdAndNodeId(String networkId, String nodeId);

    List<ConfigEntity> findByNetworkId(String networkId);

    void deleteByNetworkId(String networkId);

    void deleteByNetworkIdAndNodeId(String networkId, String nodeId);
}
