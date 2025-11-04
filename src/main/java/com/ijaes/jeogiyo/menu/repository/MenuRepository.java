package com.ijaes.jeogiyo.menu.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ijaes.jeogiyo.menu.entity.Menu;
import com.ijaes.jeogiyo.store.entity.Store;

public interface MenuRepository extends JpaRepository<Menu, UUID>, MenuRepositoryCustom {
	UUID store(Store store);
}
