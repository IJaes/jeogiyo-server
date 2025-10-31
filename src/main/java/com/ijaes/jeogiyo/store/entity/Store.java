package com.ijaes.jeogiyo.store.entity;

import java.util.UUID;

import com.ijaes.jeogiyo.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "j_store")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Store extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String businessNumber;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Category category;

	@Column(nullable = false)
	private Double rate;

	@Column(nullable = false)
	private UUID ownerId;

	public void updateName(String newName) {
		this.name = newName;
	}

	public void updateAddress(String newAddress) {
		this.address = newAddress;
	}

	public void updateDescription(String newDescription) {
		this.description = newDescription;
	}

	public void updateCategory(Category newCategory) {
		this.category = newCategory;
	}

	public void updateRate(Double newRate) {
		this.rate = newRate;
	}
}
