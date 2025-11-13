package com.ijaes.jeogiyo.menu.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ijaes.jeogiyo.menu.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, UUID>, MenuRepositoryCustom {
}
