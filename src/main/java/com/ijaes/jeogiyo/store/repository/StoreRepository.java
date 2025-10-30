package com.ijaes.jeogiyo.store.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ijaes.jeogiyo.store.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

}
