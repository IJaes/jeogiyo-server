package com.ijaes.jeogiyo.store.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ijaes.jeogiyo.store.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID>, StoreRepositoryCustom {

	boolean existsByOwnerId(UUID ownerId);

	Optional<Store> findByOwnerId(UUID id);

	Page<Store> findAll(Pageable pageable);
}
