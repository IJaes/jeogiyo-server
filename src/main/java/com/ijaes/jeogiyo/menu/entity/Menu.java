package com.ijaes.jeogiyo.menu.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ijaes.jeogiyo.common.entity.BaseEntity;
import com.ijaes.jeogiyo.store.entity.Store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "j_menu")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private Integer price;

	public void update(String name, String description, Integer price) {
		if (name != null) {
			this.name = name;
		}
		if (description != null) {
			this.description = description;
		}
		if (price != null) {
			this.price = price;
		}
	}

	public void softDelete() {
		this.setDeletedAt(LocalDateTime.now());
	}
}
