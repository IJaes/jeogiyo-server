package com.ijaes.jeogiyo.menu.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ijaes.jeogiyo.menu.entity.Menu;

public interface MenuRepositoryCustom {

	List<Menu> findByOwnerId(UUID ownerId);

	Optional<Menu> findByIdAndOwnerId(UUID menuId, UUID ownerId);

	Page<Menu> findAllNotDeleted(Pageable pageable);
}
