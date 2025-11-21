package icu.debug.net.wg.storage.sqlite.repository;

import icu.debug.net.wg.storage.sqlite.entity.NetworkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NetworkRepository extends JpaRepository<NetworkEntity, String> {
}
