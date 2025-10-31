package com.ijaes.jeogiyo.store.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Category {
	KOREAN("KOREAN"),
	JAPANESE("JAPANESE"),
	CHINESE("CHINESE"),
	ITALIAN("ITALIAN");

	private final String category;

	public String getCategory() {
		return category;
	}
}
